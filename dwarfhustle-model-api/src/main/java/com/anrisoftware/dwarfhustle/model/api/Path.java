package com.anrisoftware.dwarfhustle.model.api;

import static com.anrisoftware.dwarfhustle.model.api.PathDirection.D;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DN;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DNE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DNW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DS;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DSE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DSW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.DW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.E;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.N;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.NE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.NW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.S;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.SE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.SW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.U;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.UE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.UN;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.UNE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.UNW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.US;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.USE;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.USW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.UW;
import static com.anrisoftware.dwarfhustle.model.api.PathDirection.W;

import java.io.Serializable;

import lombok.Data;

/**
 * Connects two {@link MapTile} map tiles.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
public abstract class Path implements Serializable {

	/**
	 * {@link PathDirection#N}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class NPath extends Path {

		private static final long serialVersionUID = -2901636271129554253L;

		public static final String TYPE = "NPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return N;
		}
	}

	/**
	 * {@link PathDirection#NE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class NePath extends Path {

		private static final long serialVersionUID = 3054151071034723680L;

		public static final String TYPE = "NePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return NE;
		}
	}

	/**
	 * {@link PathDirection#E}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class EPath extends Path {

		private static final long serialVersionUID = -8253392308297880120L;

		public static final String TYPE = "EPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return E;
		}
	}

	/**
	 * {@link PathDirection#SE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class SePath extends Path {

		private static final long serialVersionUID = 2689371414754700945L;

		public static final String TYPE = "SePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return SE;
		}
	}

	/**
	 * {@link PathDirection#S}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class SPath extends Path {

		private static final long serialVersionUID = 3704178211401371110L;

		public static final String TYPE = "SPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return S;
		}
	}

	/**
	 * {@link PathDirection#SW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class SwPath extends Path {

		private static final long serialVersionUID = 7539263062673888640L;

		public static final String TYPE = "SwPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return SW;
		}
	}

	/**
	 * {@link PathDirection#W}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class WPath extends Path {

		private static final long serialVersionUID = -5702503171075269375L;

		public static final String TYPE = "WPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return W;
		}
	}

	/**
	 * {@link PathDirection#NW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class NwPath extends Path {

		private static final long serialVersionUID = 86841775509001362L;

		public static final String TYPE = "NwPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return NW;
		}
	}

	/**
	 * {@link PathDirection#UN}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UnPath extends Path {

		private static final long serialVersionUID = -8366319581548904499L;

		public static final String TYPE = "UnPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return UN;
		}
	}

	/**
	 * {@link PathDirection#UNE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UnePath extends Path {

		private static final long serialVersionUID = 8280591626858337383L;

		public static final String TYPE = "UnePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return UNE;
		}
	}

	/**
	 * {@link PathDirection#UE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UePath extends Path {

		private static final long serialVersionUID = -8100466157906045135L;

		public static final String TYPE = "UePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return UE;
		}
	}

	/**
	 * {@link PathDirection#USE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UsePath extends Path {

		private static final long serialVersionUID = -1835607062781733309L;

		public static final String TYPE = "UsePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return USE;
		}
	}

	/**
	 * {@link PathDirection#US}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UsPath extends Path {

		private static final long serialVersionUID = -6261819538867951809L;

		public static final String TYPE = "UsPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return US;
		}
	}

	/**
	 * {@link PathDirection#USW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UswPath extends Path {

		private static final long serialVersionUID = -7468805470380164294L;

		public static final String TYPE = "UswPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return USW;
		}
	}

	/**
	 * {@link PathDirection#UW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UwPath extends Path {

		private static final long serialVersionUID = -6081117030888839081L;

		public static final String TYPE = "UwPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return UW;
		}
	}

	/**
	 * {@link PathDirection#UNW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UnwPath extends Path {

		private static final long serialVersionUID = -8105410359020692331L;

		public static final String TYPE = "UnwPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return UNW;
		}
	}

	/**
	 * {@link PathDirection#DN}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DnPath extends Path {

		private static final long serialVersionUID = -5051342296074622669L;

		public static final String TYPE = "DnPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DN;
		}
	}

	/**
	 * {@link PathDirection#DNE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DnePath extends Path {

		private static final long serialVersionUID = 5753439966867824366L;

		public static final String TYPE = "DnePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DNE;
		}
	}

	/**
	 * {@link PathDirection#DE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DePath extends Path {

		private static final long serialVersionUID = -3361534082689068815L;

		public static final String TYPE = "DePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DE;
		}
	}

	/**
	 * {@link PathDirection#DSE}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DsePath extends Path {

		private static final long serialVersionUID = -1219732939992312616L;

		public static final String TYPE = "DsePath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DSE;
		}
	}

	/**
	 * {@link PathDirection#DS}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DsPath extends Path {

		private static final long serialVersionUID = -399063499489823704L;

		public static final String TYPE = "DsPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DS;
		}
	}

	/**
	 * {@link PathDirection#DSW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DswPath extends Path {

		private static final long serialVersionUID = 3551373555482429126L;

		public static final String TYPE = "DswPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DSW;
		}
	}

	/**
	 * {@link PathDirection#DW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DwPath extends Path {

		private static final long serialVersionUID = 4118628586618935458L;

		public static final String TYPE = "DwPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DW;
		}
	}

	/**
	 * {@link PathDirection#DNW}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DnwPath extends Path {

		private static final long serialVersionUID = -3377767237140056495L;

		public static final String TYPE = "DnwPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return DNW;
		}
	}

	/**
	 * {@link PathDirection#U}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class UPath extends Path {

		private static final long serialVersionUID = 4045875178934642770L;

		public static final String TYPE = "UPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return U;
		}
	}

	/**
	 * {@link PathDirection#D}
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class DPath extends Path {

		private static final long serialVersionUID = -9040160405673541825L;

		public static final String TYPE = "DPath";

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public PathDirection getDirection() {
			return D;
		}
	}

	private static final long serialVersionUID = 7665488179490574548L;

	public static final String TYPE = "Path";

	public long mapTile;

	public abstract String getType();

	public abstract PathDirection getDirection();

}
