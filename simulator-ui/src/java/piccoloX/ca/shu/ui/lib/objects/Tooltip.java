package ca.shu.ui.lib.objects;

import java.util.Collection;
import java.util.Vector;

import ca.shu.ui.lib.world.impl.WorldObjectImpl;


public abstract class Tooltip extends WorldObjectImpl {
	Collection<Button> buttons;

	WorldObjectImpl buttonsNode;

	public Tooltip() {
		super();
		buttons = new Vector<Button>();
		this.setFrameVisible(false);
		this.setDraggable(false);

		// this.addInputEventListener(new MouseDetector(this));
		getLayoutManager().setLeftPadding(0);
		getLayoutManager().setVerticalPadding(0);
		buttonsNode = new WorldObjectImpl();
		buttonsNode.setFrameVisible(false);
		buttonsNode.setDraggable(false);
		
		initButtons();

		addToLayout(buttonsNode);

		// this.setF
	}

	protected Button addButton(Button btn) {
		btn.setOffset(xPos, yPos);
		xPos += btn.getWidth() + 10;

		buttons.add(btn);
		buttonsNode.addChild(btn);

		buttonsNode.setBounds(buttonsNode.getFullBounds());
		
		return btn;

	}

	
	
	@Override
	public Tooltip getTooltipObject() {
		return null;
	}

	protected void initButtons() {
		
	}

	double xPos = 0;

	double yPos = 0;

}
