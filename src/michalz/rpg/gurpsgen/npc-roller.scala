package michalz.rpg.gurpsgen

import java.io.File
import scala.util.CommandLineParser


given CommandLineParser.FromString[File] with
  def fromString(s: String): File = new File(s)


@main
def generateNpc(inputFile: File): Unit =
  parseYaml(inputFile)