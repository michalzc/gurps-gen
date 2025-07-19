package michalz.rpg.gurpsgen.generator.ce
import cats.Applicative
import cats.Monad
import cats.effect.std.Random
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.semigroup.*

import michalz.rpg.gurpsgen.model.GeneratedTraits
import michalz.rpg.gurpsgen.model.TemplateChoice
import michalz.rpg.gurpsgen.model.TemplateElement
import michalz.rpg.gurpsgen.model.TemplateGroup
import michalz.rpg.gurpsgen.model.TemplateTrait

extension (templateTrait: TemplateTrait)
  def generate[F[_]: Applicative]: F[GeneratedTraits] =
    Applicative[F].pure(GeneratedTraits.fromTemplateTrait(templateTrait))

extension (templateGroup: TemplateGroup)
  def generate[F[_]: Monad: Random]: F[GeneratedTraits] =
    Random[F].shuffleList(templateGroup.elements).flatMap {
      case Nil       => GeneratedTraits.empty.pure
      case elem :: _ => elem.generateOne
    }

  def generate[F[_]: Monad: Random](budget: Int): F[GeneratedTraits] = {
    val shuffled: F[List[TemplateElement]] = Random[F].shuffleList(templateGroup.elements)
    def loop(result: F[GeneratedTraits], remains: List[TemplateElement]): F[GeneratedTraits] = remains match {
      case Nil => result
      case elem :: rest => result.flatMap { res =>
        elem.generateOne.flatMap { tmpRes =>
          val newRes = res |+| tmpRes
          if(newRes.totalCost > budget) res.pure[F]
          else loop(newRes.pure[F], rest)
        }
      }
    }

    shuffled.flatMap(lst => loop(GeneratedTraits.empty.pure[F], lst))
  }

end extension

extension (templateChoice: TemplateChoice)
  def generate[F[_]: Monad: Random]: F[GeneratedTraits] =
    templateChoice.choices.generateWithBudget(templateChoice.points)

extension (templateElement: TemplateElement)
  def generateOne[F[_]: Monad: Random]: F[GeneratedTraits] = templateElement match
    case templateTrait: TemplateTrait   => templateTrait.generate[F]
    case templateGroup: TemplateGroup   => templateGroup.generate[F]
    case templateChoice: TemplateChoice => templateChoice.generate[F]

  def generateWithBudget[F[_]: Monad: Random](budget: Int): F[GeneratedTraits] = templateElement match
    case templateTrait: TemplateTrait   => templateTrait.generate[F]
    case templateGroup: TemplateGroup   => templateGroup.generate[F](budget)
    case templateChoice: TemplateChoice => templateChoice.generate[F]
