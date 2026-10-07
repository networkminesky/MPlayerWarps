package dev.triumphteam.gui.guis;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.exception.GuiException;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.components.util.Legacy;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BaseGui implements InventoryHolder {
    private static final Plugin plugin = JavaPlugin.getProvidingPlugin(BaseGui.class);
    private final GuiFiller filler = new GuiFiller(this);
    private final Map<Integer, GuiItem> guiItems;
    private final Map<Integer, GuiAction<InventoryClickEvent>> slotActions;
    private final Set<InteractionModifier> interactionModifiers;
    private Inventory inventory;
    private String title;
    private int rows = 1;
    private GuiType guiType;
    private GuiAction<InventoryClickEvent> defaultClickAction;
    private GuiAction<InventoryClickEvent> defaultTopClickAction;
    private GuiAction<InventoryClickEvent> playerInventoryAction;
    private GuiAction<InventoryDragEvent> dragAction;
    private GuiAction<InventoryCloseEvent> closeGuiAction;
    private GuiAction<InventoryOpenEvent> openGuiAction;
    private GuiAction<InventoryClickEvent> outsideClickAction;
    private boolean updating;
    private boolean runCloseAction;
    private boolean runOpenAction;

    public BaseGui(int rows, @NotNull String title, @NotNull Set<InteractionModifier> interactionModifiers) {
        this.guiType = GuiType.CHEST;
        this.runCloseAction = true;
        this.runOpenAction = true;
        int finalRows = rows;
        if (rows < 1 || rows > 6) {
            finalRows = 1;
        }

        this.rows = finalRows;
        this.interactionModifiers = this.safeCopyOf(interactionModifiers);
        this.title = title;
        int inventorySize = this.rows * 9;
        this.inventory = Bukkit.createInventory(this, inventorySize, title);
        this.slotActions = new LinkedHashMap<>(inventorySize);
        this.guiItems = new LinkedHashMap<>(inventorySize);
    }

    public BaseGui(@NotNull GuiType guiType, @NotNull String title, @NotNull Set<InteractionModifier> interactionModifiers) {
        this.guiType = GuiType.CHEST;
        this.runCloseAction = true;
        this.runOpenAction = true;
        this.guiType = guiType;
        this.interactionModifiers = this.safeCopyOf(interactionModifiers);
        this.title = title;
        int inventorySize = guiType.getLimit();
        this.inventory = Bukkit.createInventory(this, guiType.getInventoryType(), title);
        this.slotActions = new LinkedHashMap<>(inventorySize);
        this.guiItems = new LinkedHashMap<>(inventorySize);
    }

    /** @deprecated */
    @Deprecated
    public BaseGui(int rows, @NotNull String title) {
        this.guiType = GuiType.CHEST;
        this.runCloseAction = true;
        this.runOpenAction = true;
        int finalRows = rows;
        if (rows < 1 || rows > 6) {
            finalRows = 1;
        }

        this.rows = finalRows;
        this.interactionModifiers = EnumSet.noneOf(InteractionModifier.class);
        this.title = title;
        this.inventory = Bukkit.createInventory(this, this.rows * 9, title);
        this.slotActions = new LinkedHashMap<>();
        this.guiItems = new LinkedHashMap<>();
    }

    /** @deprecated */
    @Deprecated
    public BaseGui(@NotNull GuiType guiType, @NotNull String title) {
        this.guiType = GuiType.CHEST;
        this.runCloseAction = true;
        this.runOpenAction = true;
        this.guiType = guiType;
        this.interactionModifiers = EnumSet.noneOf(InteractionModifier.class);
        this.title = title;
        this.inventory = Bukkit.createInventory(this, this.guiType.getInventoryType(), title);
        this.slotActions = new LinkedHashMap<>();
        this.guiItems = new LinkedHashMap<>();
    }

    private @NotNull Set<InteractionModifier> safeCopyOf(@NotNull Set<InteractionModifier> set) {
        return set.isEmpty() ? EnumSet.noneOf(InteractionModifier.class) : EnumSet.copyOf(set);
    }

    /** @deprecated */
    @Deprecated
    public @NotNull String getTitle() {
        return this.title;
    }

    public @NotNull Component title() {
        return Legacy.SERIALIZER.deserialize(this.title);
    }

    public void setItem(int slot, @NotNull GuiItem guiItem) {
        this.validateSlot(slot);
        this.guiItems.put(slot, guiItem);
    }

    public void removeItem(@NotNull GuiItem item) {
        Optional<Map.Entry<Integer, GuiItem>> entry = this.guiItems.entrySet().stream().filter((it) -> it.getValue().equals(item)).findFirst();
        entry.ifPresent((it) -> {
            this.guiItems.remove(it.getKey());
            this.inventory.remove(it.getValue().getItemStack());
        });
    }

    public void removeItem(@NotNull ItemStack item) {
        Optional<Map.Entry<Integer, GuiItem>> entry = this.guiItems.entrySet().stream().filter((it) -> it.getValue().getItemStack().equals(item)).findFirst();
        entry.ifPresent((it) -> {
            this.guiItems.remove(it.getKey());
            this.inventory.remove(item);
        });
    }

    public void removeItem(int slot) {
        this.validateSlot(slot);
        this.guiItems.remove(slot);
        this.inventory.setItem(slot, null);
    }

    public void removeItem(int row, int col) {
        this.removeItem(this.getSlotFromRowCol(row, col));
    }

    public void setItem(@NotNull List<Integer> slots, @NotNull GuiItem guiItem) {
        for (int slot : slots) {
            this.setItem(slot, guiItem);
        }
    }

    public void setItem(int row, int col, @NotNull GuiItem guiItem) {
        this.setItem(this.getSlotFromRowCol(row, col), guiItem);
    }

    public void addItem(GuiItem... items) {
        this.addItem(false, items);
    }

    public void addItem(boolean expandIfFull, GuiItem... items) {
        List<GuiItem> notAddedItems = new ArrayList<>();

        for (GuiItem guiItem : items) {
            for (int slot = 0; slot < this.rows * 9; ++slot) {
                if (this.guiItems.get(slot) == null) {
                    this.guiItems.put(slot, guiItem);
                    break;
                }

                if (slot == this.rows * 9 - 1) {
                    notAddedItems.add(guiItem);
                }
            }
        }

        if (expandIfFull && this.rows < 6 && !notAddedItems.isEmpty() && (this.guiType == null || this.guiType == GuiType.CHEST)) {
            ++this.rows;
            this.inventory = Bukkit.createInventory(this, this.rows * 9, this.title);
            this.update();
            this.addItem(true, notAddedItems.toArray(new GuiItem[0]));
        }
    }

    public void addSlotAction(int slot, @Nullable GuiAction<@NotNull InventoryClickEvent> slotAction) {
        this.validateSlot(slot);
        this.slotActions.put(slot, slotAction);
    }

    public void addSlotAction(int row, int col, @Nullable GuiAction<@NotNull InventoryClickEvent> slotAction) {
        this.addSlotAction(this.getSlotFromRowCol(row, col), slotAction);
    }

    public @Nullable GuiItem getGuiItem(int slot) {
        return this.guiItems.get(slot);
    }

    public boolean isUpdating() {
        return this.updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public void open(@NotNull HumanEntity player) {
        if (!player.isSleeping()) {
            this.inventory.clear();
            this.populateGui();
            player.getScheduler().run(plugin, (pTask) -> {
                player.openInventory(this.inventory);
            }, null);
        }
    }

    public void close(@NotNull HumanEntity player) {
        this.close(player, true);
    }

    public void close(@NotNull HumanEntity player, boolean runCloseAction) {
        try {
            // Suporte para Canvas / Folia / Paper moderno (EntityScheduler)
            player.getScheduler().runDelayed(plugin, task -> {
                this.runCloseAction = runCloseAction;
                player.closeInventory();
                this.runCloseAction = true;
            }, null, 2L);
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            // Fallback para Spigot / Paper legado
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                this.runCloseAction = runCloseAction;
                player.closeInventory();
                this.runCloseAction = true;
            }, 2L);
        }
    }

    public void update() {
        this.inventory.clear();
        this.populateGui();

        for (HumanEntity viewer : new ArrayList<>(this.inventory.getViewers())) {
            ((Player) viewer).updateInventory();
        }
    }

    @Contract("_ -> this")
    public @NotNull BaseGui updateTitle(@NotNull String title) {
        this.updating = true;
        List<HumanEntity> viewers = new ArrayList<>(this.inventory.getViewers());
        this.inventory = Bukkit.createInventory(this, this.inventory.getSize(), title);

        for (HumanEntity player : viewers) {
            this.open(player);
        }

        this.updating = false;
        this.title = title;
        return this;
    }

    public void updateItem(int slot, @NotNull ItemStack itemStack) {
        GuiItem guiItem = this.guiItems.get(slot);
        if (guiItem == null) {
            this.updateItem(slot, new GuiItem(itemStack));
        } else {
            guiItem.setItemStack(itemStack);
            this.updateItem(slot, guiItem);
        }
    }

    public void updateItem(int row, int col, @NotNull ItemStack itemStack) {
        this.updateItem(this.getSlotFromRowCol(row, col), itemStack);
    }

    public void updateItem(int slot, @NotNull GuiItem item) {
        this.guiItems.put(slot, item);
        this.inventory.setItem(slot, item.getItemStack());
    }

    public void updateItem(int row, int col, @NotNull GuiItem item) {
        this.updateItem(this.getSlotFromRowCol(row, col), item);
    }

    @Contract(" -> this")
    public @NotNull BaseGui disableItemPlace() {
        this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui disableItemTake() {
        this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui disableItemSwap() {
        this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui disableItemDrop() {
        this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui disableOtherActions() {
        this.interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui disableAllInteractions() {
        this.interactionModifiers.addAll(InteractionModifier.VALUES);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui enableItemPlace() {
        this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui enableItemTake() {
        this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui enableItemSwap() {
        this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui enableItemDrop() {
        this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui enableOtherActions() {
        this.interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return this;
    }

    @Contract(" -> this")
    public @NotNull BaseGui enableAllInteractions() {
        this.interactionModifiers.clear();
        return this;
    }

    public boolean allInteractionsDisabled() {
        return this.interactionModifiers.size() == InteractionModifier.VALUES.size();
    }

    public boolean canPlaceItems() {
        return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_PLACE);
    }

    public boolean canTakeItems() {
        return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_TAKE);
    }

    public boolean canSwapItems() {
        return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_SWAP);
    }

    public boolean canDropItems() {
        return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_DROP);
    }

    public boolean allowsOtherActions() {
        return !this.interactionModifiers.contains(InteractionModifier.PREVENT_OTHER_ACTIONS);
    }

    public @NotNull GuiFiller getFiller() {
        return this.filler;
    }

    public @NotNull Map<@NotNull Integer, @NotNull GuiItem> getGuiItems() {
        return this.guiItems;
    }

    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    public int getRows() {
        return this.rows;
    }

    public @NotNull GuiType guiType() {
        return this.guiType;
    }

    @Nullable GuiAction<InventoryClickEvent> getDefaultClickAction() {
        return this.defaultClickAction;
    }

    public void setDefaultClickAction(@Nullable GuiAction<@NotNull InventoryClickEvent> defaultClickAction) {
        this.defaultClickAction = defaultClickAction;
    }

    @Nullable GuiAction<InventoryClickEvent> getDefaultTopClickAction() {
        return this.defaultTopClickAction;
    }

    public void setDefaultTopClickAction(@Nullable GuiAction<@NotNull InventoryClickEvent> defaultTopClickAction) {
        this.defaultTopClickAction = defaultTopClickAction;
    }

    @Nullable GuiAction<InventoryClickEvent> getPlayerInventoryAction() {
        return this.playerInventoryAction;
    }

    public void setPlayerInventoryAction(@Nullable GuiAction<@NotNull InventoryClickEvent> playerInventoryAction) {
        this.playerInventoryAction = playerInventoryAction;
    }

    @Nullable GuiAction<InventoryDragEvent> getDragAction() {
        return this.dragAction;
    }

    public void setDragAction(@Nullable GuiAction<@NotNull InventoryDragEvent> dragAction) {
        this.dragAction = dragAction;
    }

    @Nullable GuiAction<InventoryCloseEvent> getCloseGuiAction() {
        return this.closeGuiAction;
    }

    public void setCloseGuiAction(@Nullable GuiAction<@NotNull InventoryCloseEvent> closeGuiAction) {
        this.closeGuiAction = closeGuiAction;
    }

    @Nullable GuiAction<InventoryOpenEvent> getOpenGuiAction() {
        return this.openGuiAction;
    }

    public void setOpenGuiAction(@Nullable GuiAction<@NotNull InventoryOpenEvent> openGuiAction) {
        this.openGuiAction = openGuiAction;
    }

    @Nullable GuiAction<InventoryClickEvent> getOutsideClickAction() {
        return this.outsideClickAction;
    }

    public void setOutsideClickAction(@Nullable GuiAction<@NotNull InventoryClickEvent> outsideClickAction) {
        this.outsideClickAction = outsideClickAction;
    }

    public @Nullable GuiAction<@NotNull InventoryClickEvent> getSlotAction(int slot) {
        return this.slotActions.get(slot);
    }

    void populateGui() {
        for (Map.Entry<Integer, GuiItem> entry : this.guiItems.entrySet()) {
            this.inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }
    }

    boolean shouldRunCloseAction() {
        return this.runCloseAction;
    }

    boolean shouldRunOpenAction() {
        return this.runOpenAction;
    }

    int getSlotFromRowCol(int row, int col) {
        return col + (row - 1) * 9 - 1;
    }

    private void validateSlot(int slot) {
        int limit = this.guiType.getLimit();
        if (this.guiType == GuiType.CHEST) {
            if (slot < 0 || slot >= this.rows * limit) {
                this.throwInvalidSlot(slot);
            }
        } else {
            if (slot < 0 || slot > limit) {
                this.throwInvalidSlot(slot);
            }
        }
    }

    private void throwInvalidSlot(int slot) {
        if (this.guiType == GuiType.CHEST) {
            throw new GuiException("Slot " + slot + " is not valid for the gui type - " + this.guiType.name() + " and rows - " + this.rows + "!");
        } else {
            throw new GuiException("Slot " + slot + " is not valid for the gui type - " + this.guiType.name() + "!");
        }
    }

    static {
        Bukkit.getPluginManager().registerEvents(new GuiListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new InteractionModifierListener(), plugin);
    }
}