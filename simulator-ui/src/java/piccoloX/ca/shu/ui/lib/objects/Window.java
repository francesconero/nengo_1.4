package ca.shu.ui.lib.objects;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;

import javax.swing.JPopupMenu;

import ca.shu.ui.lib.Style.Style;
import ca.shu.ui.lib.handlers.EventConsumer;
import ca.shu.ui.lib.util.UIEnvironment;
import ca.shu.ui.lib.util.Util;
import ca.shu.ui.lib.world.Interactable;
import ca.shu.ui.lib.world.WorldObject;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PClip;

/**
 * A Window which can be minimized, normal, maximized and closed. A Window wraps
 * another world object which contains content displayed in the window.
 * 
 * @author Shu Wu
 */
public class Window extends WorldObject implements Interactable {
	private static final int DEFAULT_HEIGHT = 400;

	private static final int DEFAULT_WIDTH = 600;

	private static final int MENU_BAR_HEIGHT = 27;

	private static final long serialVersionUID = 1L;

	public static final WindowState WINDOW_STATE_DEFAULT = WindowState.NORMAL;

	private final MenuBar menubar;

	private Border myBorder;

	private PClip myClippingRectangle;

	private final WorldObject myContent;

	private EventConsumer myEventConsumer;

	private final WeakReference<WorldObject> mySourceRef;;

	private RectangularEdge mySourceShadow = null;

	private WindowState myState = WINDOW_STATE_DEFAULT;

	private WindowState savedWindowState = WINDOW_STATE_DEFAULT;

	private PBounds savedWindowBounds;

	private Point2D savedWindowOffset;

	/**
	 * @param source
	 *            parent Node to attach this Window to
	 * @param content
	 *            Node containing the contents of this Window
	 */
	public Window(WorldObject source, WorldObject content) {
		super();
		mySourceRef = new WeakReference<WorldObject>(source);

		this.myContent = content;

		menubar = new MenuBar(this);

		myClippingRectangle = new PClip();
		myClippingRectangle.addChild(content);
		myClippingRectangle.setPaint(Style.COLOR_BACKGROUND);
		myBorder = new Border(this, Style.COLOR_FOREGROUND);

		addChild(myClippingRectangle);
		addChild(menubar);
		addChild(myBorder);

		windowStateChanged();

	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		menubar.setBounds(0, 0, getWidth(), MENU_BAR_HEIGHT);

		myContent.setBounds(0, 0, getWidth() - 4, getHeight() - 4
				- MENU_BAR_HEIGHT);
		myContent.setOffset(2, 2 + MENU_BAR_HEIGHT);

		myClippingRectangle.setPathToRectangle((float) getX(), (float) getY(),
				(float) getWidth(), (float) getHeight());

	}

	protected void maximizeBounds() {
		setOffset(0, 0);
		setBounds(parentToLocal(getParent().getBounds()));
	}

	@Override
	protected void parentBoundsChanged() {
		super.parentBoundsChanged();

		if (myState == WindowState.MAXIMIZED) {
			maximizeBounds();
		}
	}

	@Override
	protected void prepareForDestroy() {
		if (mySourceShadow != null) {
			mySourceShadow.destroy();
			mySourceShadow = null;
		}

		myContent.destroy();
		super.prepareForDestroy();
	}

	protected void windowStateChanged() {
		menubar.updateButtons();
		switch (myState) {
		case MAXIMIZED:

			if (mySourceShadow != null) {
				mySourceShadow.destroy();
				mySourceShadow = null;
			}
			myBorder.setVisible(false);
			UIEnvironment.getInstance().addWindow(this);

			if (myEventConsumer == null) {
				myEventConsumer = new EventConsumer();
				addInputEventListener(myEventConsumer);
			}			
			
			maximizeBounds();

			BoundsHandle.removeBoundsHandlesFrom(this);
			break;
		case NORMAL:
			WorldObject source = mySourceRef.get();

			if (savedWindowBounds != null) {
				setBounds(savedWindowBounds);
				setOffset(savedWindowOffset);
			} else {
				setWidth(DEFAULT_WIDTH);
				setHeight(DEFAULT_HEIGHT);
				if (source != null) {
					setOffset((getWidth() - source.getWidth()) / -2f, source
							.getHeight() + 20);
				}

			}
			if (source != null) {
				if (myEventConsumer != null) {
					removeInputEventListener(myEventConsumer);
					myEventConsumer = null;
				}

				source.addChild(this);
				myBorder.setVisible(true);

				BoundsHandle.addBoundsHandlesTo(this);
				if (mySourceShadow == null) {

					mySourceShadow = new RectangularEdge(source, this);
					source.addChild(0, mySourceShadow);
				}
			} else {
				Util
						.Assert(false,
								"Window still active after source destroyed");
			}

			break;
		case MINIMIZED:

			if (mySourceShadow != null) {
				mySourceShadow.destroy();
				mySourceShadow = null;
			}
			if (mySourceRef.get() != null) {
				mySourceRef.get().addChild(this);
			}
			break;
		}
		if (myState == WindowState.MINIMIZED) {
			setVisible(false);
			setChildrenPickable(false);
			setPickable(false);
		} else {
			setVisible(true);
			setChildrenPickable(true);
			setPickable(true);
		}
		layoutChildren();
	}

	/**
	 * Closes the window
	 */
	public void close() {
		destroy();
	}

	/**
	 * Increases the size of the window through state transitions
	 */
	public void cycleVisibleWindowState() {
		switch (myState) {
		case MAXIMIZED:
			setWindowState(WindowState.NORMAL);
			break;
		case NORMAL:
			setWindowState(WindowState.MAXIMIZED);
			break;
		case MINIMIZED:
			setWindowState(WindowState.NORMAL);
		}

	}

	@Override
	public String getName() {
		return myContent.getName();
	}

	/**
	 * @return Node representing the contents of the Window
	 */
	public WorldObject getWindowContent() {
		return myContent;
	}

	public WindowState getWindowState() {
		return myState;
	}

	@Override
	public void moveToFront() {
		super.moveToFront();
		if (mySourceRef.get() != null) {
			mySourceRef.get().moveToFront();
		}
	}

	public void restoreSavedWindow() {
		setWindowState(savedWindowState);
	}

	public void setWindowState(WindowState state) {

		if (state != myState) {

			/*
			 * Saves the window bounds and offset
			 */
			if (myState == WindowState.NORMAL) {
				savedWindowBounds = getBounds();
				savedWindowOffset = getOffset();
			}
			/*
			 * Saves the previous window state
			 */
			if (state == WindowState.MINIMIZED) {
				savedWindowState = myState;
			}

			myState = state;
			windowStateChanged();
		}
	}

	public JPopupMenu showContextMenu() {
		if (getWindowContent() instanceof Interactable) {
			return ((Interactable) (getWindowContent())).showContextMenu();
		}
		return null;
	}

	public static enum WindowState {
		MAXIMIZED, MINIMIZED, NORMAL
	}

}

class MenuBar extends PPath implements PInputEventListener {

	private static final long serialVersionUID = 1L;
	private AbstractButton maximizeButton, minimizeButton, closeButton,
			normalButton;
	private PXText title;

	private Window myWindow;

	public MenuBar(Window window) {
		super(new Rectangle2D.Double(0, 0, 1, 1));
		this.myWindow = window;

		addInputEventListener(this);
		init();
	}

	private void init() {
		title = new PXText(myWindow.getName());
		title.setFont(Style.FONT_LARGE);
		addChild(title);

		normalButton = new ImageButton("images/icons/restore.gif",
				new Runnable() {
					public void run() {
						myWindow.setWindowState(Window.WindowState.NORMAL);
					}
				});

		maximizeButton = new ImageButton("images/icons/maximize.gif",
				new Runnable() {
					public void run() {
						myWindow.setWindowState(Window.WindowState.MAXIMIZED);
					}
				});

		minimizeButton = new ImageButton("images/icons/minimize.gif",
				new Runnable() {
					public void run() {
						myWindow.setWindowState(Window.WindowState.MINIMIZED);
					}
				});

		closeButton = new ImageButton("images/icons/close.gif", new Runnable() {
			public void run() {
				myWindow.close();
			}
		});

		addChild(maximizeButton);
		addChild(normalButton);
		addChild(minimizeButton);
		addChild(closeButton);
		setPaint(Style.COLOR_BACKGROUND2);
	}

	public void updateButtons() {
		boolean isWindowMaximized = (myWindow.getWindowState() == Window.WindowState.MAXIMIZED);

		if (isWindowMaximized) {
			maximizeButton.removeFromParent();
			addChild(normalButton);
		} else {
			normalButton.removeFromParent();
			addChild(maximizeButton);
		}

	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		title.setBounds(4, 3, getWidth(), getHeight());

		double buttonX = getWidth() - closeButton.getWidth();
		closeButton.setOffset(buttonX, 0);
		buttonX -= closeButton.getWidth();
		maximizeButton.setOffset(buttonX, 0);
		normalButton.setOffset(buttonX, 0);
		buttonX -= minimizeButton.getWidth();
		minimizeButton.setOffset(buttonX, 0);
	}

	public void processEvent(PInputEvent event, int type) {
		if (type == MouseEvent.MOUSE_CLICKED && event.getClickCount() == 2) {
			myWindow.cycleVisibleWindowState();
		}
	}

}
