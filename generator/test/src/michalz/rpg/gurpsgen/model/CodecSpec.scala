package michalz.rpg.gurpsgen.model

import scala.io.Source
import scala.util.Using

import io.circe.Json
import io.circe.syntax.*
import io.circe.yaml.v12.parser.parse

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import michalz.rpg.gurpsgen.codec.given
import michalz.rpg.gurpsgen.utils.orThrow

class CodecSpec extends AnyFreeSpec, Matchers, EitherValues, LazyLogging {

  "A codec for" - {
    "TemplateTrait" - {
      "should deserialize" - {
        "proper line" in {
          val json = "ST 11 [10]".asJson
          json.as[TemplateElement].value shouldEqual TemplateTrait("ST 11", 10)
        }

        "line with negative cost" in {
          val json = "Code of Honor 10 [-10]".asJson
          json.as[TemplateElement].value shouldEqual TemplateTrait("Code of Honor 10", -10)
        }
      }
    }

    "TemplateGroup" - {
      "should deserialize" - {
        "simple list of elements" in {
          val json = Json.arr("Magic Resistance 1 [2]".asJson, "Status 1 [5]".asJson, "Code of Honor 10 [-10]".asJson)
          json.as[TemplateElement].value shouldEqual TemplateGroup(
            List(TemplateTrait("Magic Resistance 1", 2), TemplateTrait("Status 1", 5), TemplateTrait("Code of Honor 10", -10))
          )
        }

        "nested list" in {
          val json = Json.arr(
            "Magic resistence [5]".asJson,
            Json.arr("Elem 1 [5]".asJson, "+2 ST [10]".asJson),
            "Other [2]".asJson
          )
          json.as[TemplateElement].value shouldEqual TemplateGroup(
            TemplateTrait("Magic resistence", 5),
            TemplateGroup(TemplateTrait("Elem 1", 5), TemplateTrait("+2 ST", 10)),
            TemplateTrait("Other", 2)
          )
        }

        "list from yaml" in withYamlFromFile("/attributes.yml") { json =>
          json.as[TemplateElement].orThrow
        }

        "simple nested list from yaml" in withYamlFromFile("/simple-nested-list.yml") { json =>
          json.as[TemplateElement].orThrow
        }

        "nested lists from yaml" in withYamlFromFile("/nested-lists.yml") { json =>
          json.as[TemplateElement].orThrow
        }
      }
    }

    "TemplateChoice" - {
      "should deserialize" - {
        "one element generator" in {
          val json = Json.obj("points" -> 20.asJson, "choices" -> "Some advantage [20]".asJson)
          json.as[TemplateElement].value shouldEqual TemplateChoice(
            points = 20,
            choices = TemplateTrait(name = "Some advantage", cost = 20)
          )
        }
      }
    }

    "NPCTemplate" - {
      "should deserialize" - {
        "simple generator from yaml file" in withYamlFromFile("/attributes-npc.yml") { json =>
          json.as[NPCTemplate].orThrow
        }

        "generator from yaml file" in withYamlFromFile("/soldier-of-fortune.yml") { json =>
          json.as[NPCTemplate].orThrow
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
