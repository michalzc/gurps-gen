package michalz.rpg.gurpsgen.model

sealed trait GeneratorElement

case class Element(name: String, cost: Int) extends GeneratorElement

case class ListElements(elements: List[GeneratorElement])
    extends GeneratorElement

case class Generator(points: Int, choices: GeneratorElement) extends GeneratorElement

case class NPCGenerator(elements: Map[String, GeneratorElement])
