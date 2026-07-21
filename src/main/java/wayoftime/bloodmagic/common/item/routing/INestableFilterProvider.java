package wayoftime.bloodmagic.common.item.routing;

/**
 * Marker matching 1.20.1's {@code INestableItemFilterProvider}: implemented by every Filter item
 * that {@code CompositeFilterItem} is allowed to hold in one of its own slots. The Composite filter
 * itself deliberately does not implement this, so a composite can't nest another composite
 * (avoiding recursive matching) - the same restriction the original's design implied.
 */
public interface INestableFilterProvider extends IFilterProvider {
}
