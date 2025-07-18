package michalz.rpg.gurpsgen.model

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
