package michalz.rpg.gurpsgen.model

case class GeneratedTraits(traits: List[String], totalCost: Int)
object GeneratedTraits:
  def fromTemplateTrait(t: TemplateTrait) = GeneratedTraits(List(t.name), t.cost)
  def empty: GeneratedTraits = GeneratedTraits(List.empty, 0)

sealed trait TemplateElement

case class TemplateTrait(name: String, cost: Int) extends TemplateElement

case class TemplateGroup(elements: List[TemplateElement]) extends TemplateElement

object TemplateGroup:
  def apply(elems: TemplateElement*): TemplateGroup =
    TemplateGroup(elems.toList)

case class TemplateChoice(points: Int, choices: TemplateElement) extends TemplateElement

case class NPCTemplate(
    elements: Map[String, TemplateElement]
)

object NPCTemplate:
  def appy(
      elems: (String, TemplateElement)*
  ): NPCTemplate = NPCTemplate(elems.toMap)
