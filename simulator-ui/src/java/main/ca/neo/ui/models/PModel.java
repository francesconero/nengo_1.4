package ca.neo.ui.models;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import ca.shu.ui.lib.handlers.IContextMenu;
import ca.shu.ui.lib.util.PopupMenuBuilder;
import ca.shu.ui.lib.util.Util;
import ca.shu.ui.lib.world.impl.WorldObjectImpl;
import edu.umd.cs.piccolo.event.PInputEvent;

public abstract class PModel extends WorldObjectImpl implements IContextMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Object model;

	WorldObjectImpl icon;
	
	

	// boolean modelCreationCancelled = false;

	/**
	 * Default constructor, model is constructed internally
	 */
	public PModel() {
		super();
		// setName(name);
	}

	public abstract String getTypeName();

	/**
	 * @param model
	 *            Model is constructed externally
	 */
	public PModel(Object model) {

		setModel(model);

	}

	public PopupMenuBuilder constructMenu() {

		PopupMenuBuilder menu = new PopupMenuBuilder(getName());

		menu.addSection("Object");

		menu.addAction(new AbstractAction("Remove from world") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				removeFromParent();
			}
		});

		return menu;
	}

	public WorldObjectImpl getIcon() {
		return icon;
	}

	public Object getModel() {
		return model;
	}


	public boolean isModelCreated() {
		return (model != null);
	}

	public void setModel(Object model) {
		this.model = model;

	}


	public JPopupMenu showPopupMenu(PInputEvent event) {
		if (model == null) {
			Util.Warning("Model is not configured yet");
			return null;
		} else {
			JPopupMenu menu = constructMenu().getJPopupMenu();

			return menu;
		}
	}

	protected void setIcon(WorldObjectImpl icon) {
		if (this.icon != null) {

			this.icon.removeFromParent();
		}

		this.icon = icon;

		this.addChild(icon);
		icon.setDraggable(false);
		// icon.setPickable(false);

		setBounds(getFullBounds());

	}

}
