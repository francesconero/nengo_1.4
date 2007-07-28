package ca.shu.ui.lib.handlers;

import java.awt.event.MouseEvent;
import java.util.ListIterator;

import ca.shu.ui.lib.objects.widgets.AutomaticFrame;
import ca.shu.ui.lib.world.IWorld;
import ca.shu.ui.lib.world.IWorldObject;
import ca.shu.ui.lib.world.impl.WorldObjectImpl;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PStack;

public class DragHandler extends PDragEventHandler {
	PPath highlightFrame = null;

	// static AutomaticFrame frame = new AutomaticFrame();

	public DragHandler() {
		super();

	}

	@Override
	protected void drag(PInputEvent event) {
		super.drag(event);
		event.setHandled(true);
		return;
	}

	@Override
	protected void endDrag(PInputEvent event) {
		IWorldObject wo = getDraggedWO();

		wo.endDrag();
		wo.popState(IWorldObject.State.IN_DRAG);
		wo.justDropped();

		super.endDrag(event);
	}

	protected WorldObjectImpl getDraggedWO() {
		return (WorldObjectImpl) getDraggedNode();
	}

	@Override
	protected void startDrag(PInputEvent event) {
		if (event.getButton() != MouseEvent.BUTTON1)
			return;

		PStack nodeStack = event.getPath().getNodeStackReference();
		ListIterator it = nodeStack.listIterator(nodeStack.size());

		/*
		 * Searches the node stack for draggable World Objects
		 */
		while (it.hasPrevious()) {
			Object node = it.previous();

			/*
			 * Do not propogate drag events beyond the borders of a world
			 */
			if (node instanceof IWorld) {
				return;
			}

			if (node instanceof WorldObjectImpl) {
				WorldObjectImpl wo = (WorldObjectImpl) node;
				if (wo.isDraggable()) {

					super.startDrag(event);
					setDraggedNode(wo);
					wo.moveToFront();
					wo.startDrag();
					wo.pushState(IWorldObject.State.IN_DRAG);

					return;
				}
			}
		}
	}
}

// /*
// * Drops a Node into a container
// *
// */
// class DropActivity extends PActivity {
// IWorldObject wo;
//
// IWorld world;
//
// public DropActivity(IWorldObject nodeToDrop, long aDuration) {
// super(aDuration, aDuration);
//
// this.wo = nodeToDrop;
// world = nodeToDrop.getWorld();
// }
//
// @Override
// protected void activityFinished() {
// // TODO Auto-generated method stub
// super.activityFinished();
// Point2D position = wo.getWorld().getPositionInGround(wo);
// wo.setOffset(position);
// world.getGround().addChildWorldObject(wo);
//
// // wo.justDropped();
// }
//
// int i = 0;
//
// @Override
// protected void activityStarted() {
// // TODO Auto-generated method stub
// super.activityStarted();
//
// // wo.animateToPositionScaleRotation(0, 0, 1, 0, 2000);
// if (wo.getWorldLayer() instanceof WorldSky) {
// // wo.animateToPositionScaleRotation(312, 67, 1, 0, 200);
//
// wo.animateToPositionScaleRotation(wo.getOffset().getX(), wo
// .getOffset().getY(), world.getGroundScale() + i++, 0,
// getDuration());
//
// } else {
// terminate(TERMINATE_WITHOUT_FINISHING);
// activityFinished();
// }
//
// }
//
// }
