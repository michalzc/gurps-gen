package michalz.rpg.gurpsgen

import java.io.File
import io.circe.yaml.v12.parser.parse
import scala.util.Using

import scala.io.Source
import michalz.rpg.gurpsgen.utils.orThrow

def parseYaml(file: File): Unit = {
  val source = Using(Source.fromFile(file)) { reader =>
    reader.mkString
  }.orThrow
  val json = parse(source).orThrow

  println(json)
}
