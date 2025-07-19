package michalz.rpg.gurpsgen.generator.ce

import cats.effect.IO
import cats.effect.std.Random
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.instances.list.*
import cats.syntax.traverse.*

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

import michalz.rpg.gurpsgen.model.GeneratedTraits
import michalz.rpg.gurpsgen.model.TemplateGroup
import michalz.rpg.gurpsgen.model.TemplateTrait

class CEGeneratorSpec extends AsyncFreeSpec, Matchers, AsyncIOSpec {

  given SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  given IO[Random[IO]] = Random.scalaUtilRandom[IO]

  "A cats-effect based generator for" - {
    "TemplateTrait should" - {
      "return simple generated row" in {
        val extected                    = GeneratedTraits(List("Elem"), 0)
        val genTrait                    = TemplateTrait("Elem", 0)
        val result: IO[GeneratedTraits] = genTrait.generateOne[IO]
        result.asserting(f => f shouldEqual extected)
      }
    }

    "TemplateGroup with lazy should" - {
      "return element from one element group" in {
        val elem     = TemplateTrait("elem", 0)
        val expected = GeneratedTraits.fromTemplateTrait(elem)
        val genTrait = TemplateGroup(elem)

        genTrait.generateWithBudget[IO](10).asserting(res => res shouldEqual expected)
      }

      "return all elements from group if below budget" in {
        val elems = List(
          TemplateTrait("trait one", 1),
          TemplateTrait("trait two", 2),
          TemplateTrait("trait three", 3)
        )
        val genTrait = TemplateGroup(elems)

        genTrait
          .generateWithBudget[IO](10)
          .asserting(_.traits should contain theSameElementsAs elems.map(_.name))
      }

      "return some elements in budget" in {
        val elems    = (1 to 10).map(num => TemplateTrait(s"Element $num", num))
        val genTrait = TemplateGroup(elems.toList)

        genTrait
          .generateWithBudget[IO](10)
          .flatTap { result =>
            result.traits.traverse(elem => debug"Generated: $elem")
          }
          .map(result => assert(result.totalCost <= 10, s"Total cost ${result} is above 10"))
      }

      "return some elements from large list in budget" in {
        val budget = 50
        val elems  = (1 to 100).map(num => {
          TemplateTrait(s"Element $num", num % 10)
        })
        val genTrait = TemplateGroup(elems.toList)

        genTrait
          .generateWithBudget[IO](budget)
          .flatTap { result =>
            debug"Total cost: ${result.totalCost}" >> result.traits
              .traverse(elem => debug"Generated: $elem")
          }
          .map(result => assert(result.totalCost <= budget, s"Total cost ${result} is above ${budget}"))
      }
    }
  }
}
