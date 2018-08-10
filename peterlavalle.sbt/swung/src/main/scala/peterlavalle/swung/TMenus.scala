package peterlavalle.swung

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JMenu, JMenuBar, JMenuItem}

trait TMenus {

	implicit class WrappedJMenuBar[J <: JMenuBar](menuBar: J) {

		def item(menu: String, item: String, path: String*)(doit: => Unit): J =
			menus.filter(_.getText == menu) match {
				case Nil =>
					menuBar.add(
						new JMenu(menu).item(item :: path.toList)(doit)
					)
					menuBar
				case List(jMenu: JMenu) =>
					jMenu.item(item :: path.toList)(doit)
					menuBar
			}

		def menus: List[JMenu] = (0 until menuBar.getMenuCount).toList.map(menuBar.getMenu)

		def item(path: List[String]): JMenuItem = {
			val head :: tail = path

			val menu: JMenu =
				menuBar.menus.filter(_.getText == head) match {
					case List(menu: JMenu) =>
						menu

					case Nil =>
						val menu: JMenu = new JMenu(head)

						menuBar.add(menu)

						menu
				}

			menu.follow(tail)
		}
	}

	implicit class WrappedJMenu[J <: JMenu](menu: J) {
		def follow(path: List[String]): JMenuItem = {
			val bits = menu.items.filter(_.getText == path.head)
			(path, bits) match {

				case (List(last: String), Nil) =>
					val menuItem: JMenuItem = new JMenuItem(last)
					menu.add(menuItem)
					menuItem

				case (List(_: String), List(found: JMenuItem)) =>
					assume(!found.isInstanceOf[JMenu])
					found

				case (head :: tail, Nil) =>
					val sub = new JMenu(head)
					menu.add(sub)
					sub.follow(tail)

				case (_ :: tail, List(found: JMenu)) =>
					found.follow(tail)
			}
		}

		def item(path: List[String])(doit: => Unit): J = {
			path match {
				case head :: Nil =>
					(items.filter(_.getText == head) match {
						case Nil =>
							val menuItem: JMenuItem = new JMenuItem(head)
							menu.add(menuItem)
							menuItem
					}).addActionListener(
						new ActionListener {
							override def actionPerformed(e: ActionEvent): Unit = doit
						}
					)
			}
			menu
		}

		def items: List[JMenuItem] =
			(0 until menu.getItemCount).toList.map(menu.getItem)
	}

}
