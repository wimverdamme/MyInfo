package my.info;

import java.io.IOException;
import java.io.Serializable;

public class Poi implements Serializable {
	private static final long serialVersionUID = 3541281213042209055L;
	private String Type;
	private int LatitudeE6;
	private int LongitudeE6;
	private int Id;
	private static int LastId=0;

	public Poi(int latitudeE6, int longitudeE6, String Type) {
		this.LongitudeE6 = longitudeE6;
		this.LatitudeE6 = latitudeE6;
		this.Type = Type;
		this.Id=LastId++;
	}

	public String getType() {
		return Type;
	}

	int getLatitudeE6() {
		return LatitudeE6;
	}

	int getLongitudeE6() {
		return LongitudeE6;
	}
	int getId() {
		return Id;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(Type);
		out.writeInt(LatitudeE6);
		out.writeInt(LongitudeE6);

	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		Type = (String) in.readObject();
		LatitudeE6 = in.readInt();
		LongitudeE6 = in.readInt();
	}
}
