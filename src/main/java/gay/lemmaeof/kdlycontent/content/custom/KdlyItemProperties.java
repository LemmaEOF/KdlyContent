package gay.lemmaeof.kdlycontent.content.custom;

import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;

import java.util.Map;
import java.util.Optional;

public record KdlyItemProperties(Optional<BarProperties> bar, Optional<ChargeProperties> charge,
								 boolean selfRemainder, Map<ItemFunctionPoint, Identifier> functions) {

	//TODO: add bar component? put in kindly components maybe?
	public record BarProperties(int barColor, String barTag, int barMax, boolean showWhenFull) {}

	public record ChargeProperties(int minChargeDuration, int maxChargeDuration, UseAction action) {}

	public enum ItemFunctionPoint {
		HIT_BLOCK("hit_block"),
		HIT_ENTITY("hit_entity"),
		USE_IN_AIR("use_in_air"),
		USE_ON_BLOCK("use_on_block"),
		USE_ON_ENTITY("use_on_entity"),
		CHARGE_RELEASE("charge_release"),
		CHARGE_FINISH("charge_finish");

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
