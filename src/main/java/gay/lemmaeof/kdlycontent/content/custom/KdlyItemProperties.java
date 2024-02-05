package gay.lemmaeof.kdlycontent.content.custom;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record KdlyItemProperties(Optional<BarProperties> bar, Optional<ChargeProperties> charge, boolean hasGlint,
								 boolean selfRemainder, List<Text> lore, Map<ItemFunctionPoint, Identifier> functions) {

	public record BarProperties(int barColor, String barTag, int barMax, boolean showWhenFull) {}

	public record ChargeProperties(int minChargeDuration, int maxChargeDuration, UseAction action) {}

	public enum ItemFunctionPoint {
		HIT_BLOCK("hitBlock"),
		HIT_ENTITY("hitEntity"),
		USE_IN_AIR("useInAir"),
		USE_ON_BLOCK("useOnBock"),
		USE_ON_ENTITY("useOnEntity"),
		CHARGE_RELEASE("chargeRelease"),
		CHARGE_FINISH("chargeFinish");

		private final String name;

		ItemFunctionPoint(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static ItemFunctionPoint forName(String name) {
			for (ItemFunctionPoint point : values()) {
				if (name.equals(point.name)) return point;
			}
			throw new IllegalArgumentException("Unknown item function point " + name);
		}
	}
}
