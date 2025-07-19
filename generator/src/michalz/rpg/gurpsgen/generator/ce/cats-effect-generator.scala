package michalz.rpg.gurpsgen.generator.ce
import cats.Applicative
import cats.Monad
import cats.effect.std.Random
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.semigroup.*

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

import michalz.rpg.gurpsgen.model.GeneratedTraits
import michalz.rpg.gurpsgen.model.TemplateChoice
import michalz.rpg.gurpsgen.model.TemplateElement
import michalz.rpg.gurpsgen.model.TemplateGroup
import michalz.rpg.gurpsgen.model.TemplateTrait

extension (result: GeneratedTraits)
  def better(other: GeneratedTraits): GeneratedTraits = if (result.totalCost > other.totalCost) result else other

extension (templateTrait: TemplateTrait)
  def generate[F[_]: Applicative]: F[GeneratedTraits] =
    Applicative[F].pure(GeneratedTraits.fromTemplateTrait(templateTrait))

extension (templateGroup: TemplateGroup)
  def generate[F[_]: Monad: Random: Logger]: F[GeneratedTraits] =
    Random[F].shuffleList(templateGroup.elements).flatMap {
      case Nil       => GeneratedTraits.empty.pure
      case elem :: _ => elem.generateOne
    }

  def generate[F[_]: Monad: Random: Logger](budget: Int, numberOfRetries: Int): F[GeneratedTraits] = {

    def loop(result: F[GeneratedTraits], remains: List[TemplateElement]): F[GeneratedTraits] = remains match {
      case Nil          => result
      case elem :: rest =>
        result.flatMap { res =>
          elem.generateOne.flatMap { tmpRes =>
            val newRes = res |+| tmpRes
            if (newRes.totalCost > budget) res.pure[F]
            else loop(newRes.pure[F], rest)
          }
        }
    }

    def retryLoop(lastResult: GeneratedTraits, retries: Int): F[GeneratedTraits] =
      val shuffled: F[List[TemplateElement]] = Random[F].shuffleList(templateGroup.elements)
      shuffled
        .flatMap(lst => loop(GeneratedTraits.empty.pure[F], lst))
        .flatTap(result => debug"Got result for ${result.totalCost} in ${retries} try")
        .flatMap {
          case result if result.totalCost == budget => debug"Perfect match" >> result.pure[F]
          case result if retries < numberOfRetries  => retryLoop(lastResult.better(result), retries + 1)
          case result                               =>
            lastResult
              .better(result)
              .pure[F]
              .flatTap(result =>
                debug"Can't fiend perfect match for ${budget} after $retries tries, give up with ${result.totalCost}"
              )
        }

    retryLoop(GeneratedTraits.empty, 1)
  }

end extension

extension (templateChoice: TemplateChoice)
  def generate[F[_]: Monad: Random: Logger]: F[GeneratedTraits] =
    templateChoice.choices.generateWithBudget(templateChoice.points)

extension (templateElement: TemplateElement)
  def generateOne[F[_]: Monad: Random: Logger]: F[GeneratedTraits] = templateElement match
    case templateTrait: TemplateTrait   => templateTrait.generate[F]
    case templateGroup: TemplateGroup   => templateGroup.generate[F]
    case templateChoice: TemplateChoice => templateChoice.generate[F]

  def generateWithBudget[F[_]: Monad: Random: Logger](budget: Int, numberOfRetries: Int = 10): F[GeneratedTraits] =
    templateElement match
      case templateTrait: TemplateTrait   => templateTrait.generate[F]
      case templateGroup: TemplateGroup   => templateGroup.generate[F](budget, numberOfRetries)
      case templateChoice: TemplateChoice => templateChoice.generate[F]
