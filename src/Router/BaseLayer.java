package Router;

public abstract class BaseLayer {
	String layerName;
	Object upperLayer;
	Object underLayer;

	public BaseLayer(String layerName) {
		this.layerName = layerName;
	}

	void setUpperLayer(Object upperLayer) {
		this.upperLayer = upperLayer;
	}

	void setUnderLayer(Object underLayer) {
		this.underLayer = underLayer;
	}

	Object getUpperLayer() {
		if ((Object) upperLayer == null) {
			System.out.println("[Object-getUnderLayer] There is no UnderLayer");
			return null;
		}

		return upperLayer;
	}

	Object getUnderLayer() {
		if ((Object) underLayer == null) {
			System.out.println("[Object-getUnderLayer] There is no UnderLayer");
			return null;
		}

		return underLayer;
	}

	String getLayerName() {
		return layerName;
	}
}