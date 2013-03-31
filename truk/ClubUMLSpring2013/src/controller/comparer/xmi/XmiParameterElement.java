package controller.comparer.xmi;

public class XmiParameterElement extends XmiBaseElement {

	public static final String DIRECTION_IN = "in";
	public static final String DIRECTION_OUT = "out";
	public static final String DIRECTION_INOUT = "inout";
	public static final String DIRECTION_RETURN = "return";

	private static final String DIRECTION_DEFAULT = DIRECTION_IN;

	private String direction;

	public XmiParameterElement(String id, String name, String type,
			String visibility, String direction) {
		super(id, name, type, visibility);

		if (direction == null) {
			this.setDirection(DIRECTION_DEFAULT);
		} else if (direction.isEmpty()) {
			this.setDirection(DIRECTION_DEFAULT);
		}
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	@Override
	public String toString() {
		return this.direction + " " + super.getVisibility() + " "
				+ super.getTypeName() + " " + super.getName();
	}

	@Override
	public boolean equals(Object object) {
		XmiParameterElement o = (XmiParameterElement) object;
		if (this.getName() == o.getName()
				&& this.getVisibility() == o.getVisibility()
				&& this.getDirection() == o.getDirection()
				&& this.getTypeName() == o.getTypeName()) {
			return true;
		} else
			return false;
	}
}