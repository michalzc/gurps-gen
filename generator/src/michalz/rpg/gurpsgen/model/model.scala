package michalz.rpg.gurpsgen.model

sealed trait GeneratorElement

case class Element(name: String, cost: Int) extends GeneratorElement

case class ListElements(elements: List[GeneratorElement]) extends GeneratorElement

object ListElements:
  def apply(elems: GeneratorElement*): ListElements =
    ListElements(elems.toList)

case class Generator(points: Int, choices: GeneratorElement) extends GeneratorElement

case class NPCGenerator(
    elements: Map[String, GeneratorElement]
)

object NPCGenerator:
  def appy(
      elems: (String, GeneratorElement)*
  ): NPCGenerator = NPCGenerator(elems.toMap)
