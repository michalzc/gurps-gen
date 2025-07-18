package michalz.rpg.gurpsgen.model

import scala.io.Source
import scala.util.Using

import io.circe.Json
import io.circe.syntax._
import io.circe.yaml.v12.parser.parse

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import michalz.rpg.gurpsgen.codec.given
import michalz.rpg.gurpsgen.utils.orThrow

class CodecSpec extends AnyFreeSpec with Matchers with EitherValues {
  "A codec for" - {
    "Element" - {
      "should deserialize" - {
        "proper line" in {
          val json = "ST 11 [10]".asJson
          json.as[GeneratorElement].value shouldEqual Element("ST 11", 10)
        }

        "line with negative cost" in {
          val json = "Code of Honor 10 [-10]".asJson
          json.as[GeneratorElement].value shouldEqual Element("Code of Honor 10", -10)
        }
      }
    }

    "ListElements" - {
      "should deserialize" - {
        "simple list of elements" in {
          val json = Json.arr("Magic Resistance 1 [2]".asJson, "Status 1 [5]".asJson, "Code of Honor 10 [-10]".asJson)
          json.as[GeneratorElement].value shouldEqual ListElements(
            List(Element("Magic Resistance 1", 2), Element("Status 1", 5), Element("Code of Honor 10", -10))
          )
        }

        "nested list" in {
          val json = Json.arr(
            "Magic resistence [5]".asJson,
            Json.arr("Elem 1 [5]".asJson, "+2 ST [10]".asJson),
            "Other [2]".asJson
          )
          json.as[GeneratorElement].value shouldEqual ListElements(
            Element("Magic resistence", 5),
            ListElements(Element("Elem 1", 5), Element("+2 ST", 10)),
            Element("Other", 2)
          )
        }

        "list from yaml" in withYamlFromFile("/attributes.yml") { json =>
          json.as[GeneratorElement].orThrow
        }

        "simple nested list from yaml" in withYamlFromFile("/simple-nested-list.yml") { json =>
          json.as[GeneratorElement].orThrow
        }

        "nested lists from yaml" in withYamlFromFile("/nested-lists.yml") { json =>
          json.as[GeneratorElement].orThrow
        }
      }
    }

    "Generator" - {
      "should deserialize" - {
        "one element generator" in {
          val json = Json.obj("points" -> 20.asJson, "choices" -> "Some advantage [20]".asJson)
          json.as[GeneratorElement].value shouldEqual Generator(
            points = 20,
            choices = Element(name = "Some advantage", cost = 20)
          )
        }
      }
    }

    "NPC Generator" - {
      "should deserialize" - {
        "simple generator from yaml file" in withYamlFromFile("/attributes-npc.yml") { json =>
          json.as[NPCGenerator].orThrow
        }
        "generator from yaml file" in withYamlFromFile("/soldier-of-fortune.yml") { json =>
          json.as[NPCGenerator].orThrow
        }
      }
    }
  }

  def withFile[A](fileName: String)(code: String => A): A =
    Using(Source.fromInputStream(getClass.getResourceAsStream(fileName))) { src =>
      code(src.mkString)
    }.orThrow

  def withYamlFromFile[A](fileName: String)(code: Json => A): A = withFile(fileName) { yamlString =>
    code(parse(yamlString).orThrow)
  }
}
