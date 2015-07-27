package eu.phiwa.dragontravel.core.payment;

import eu.phiwa.dragontravel.core.DragonTravelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ResourcesPaymentHandler implements PaymentHandler {

    public ResourcesPaymentHandler() {
    }

    @Override
    public boolean setup() {
        return DragonTravelMain.getInstance().getConfigHandler().isByResources();
    }

    @Override
    public String toString() {
        return ChatColor.BLUE + "items";
    }

    @Override
    public boolean chargePlayer(ChargeType type, Player player) {
        if (type.hasNoCostPermission(player)) {
            player.sendMessage(DragonTravelMain.getInstance().getMessagesHandler().getMessage("Messages.Payment.Free"));
            return true;
        }

        int amount;
        switch (type) {
            case TRAVEL_TOSTATION:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.toStation");
                break;
            case TRAVEL_TORANDOM:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.toRandom");
                break;
            case TRAVEL_TOPLAYER:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.toPlayer");
                break;
            case TRAVEL_TOCOORDINATES:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.toCoordinates");
                break;
            case TRAVEL_TOHOME:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.toHome");
                break;
            case TRAVEL_TOFACTIONHOME:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.toFactionhome");
                break;
            case SETHOME:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.setHome");
                break;
            case FLIGHT:
                amount = DragonTravelMain.getInstance().getConfig().getInt("Payment.Resources.Prices.Flight");
                break;
            default:
                throw new UnsupportedOperationException("ResourcesPaymentHandler doesn't know how to deal with a ChargeType of " + type.name() + ". Fix immediately!");
        }

        return removeItems(player, amount);
    }

    @Override
    public boolean chargePlayerExact(ChargeType type, Player player, double customCost) {
        if (type.hasNoCostPermission(player)) {
            player.sendMessage(DragonTravelMain.getInstance().getMessagesHandler().getMessage("Messages.Payment.Free"));
            return true;
        }

        return removeItems(player, (int) customCost);
    }

    @SuppressWarnings("deprecation")
    private boolean removeItems(Player player, int amount) {
        Inventory inv = player.getInventory();
        ItemStack item = new ItemStack(DragonTravelMain.getInstance().getConfigHandler().getPaymentItemType(), amount);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', DragonTravelMain.getInstance().getConfigHandler().getPaymentItemName()));
        item.setItemMeta(im);
        if (inv.containsAtLeast(item, amount)) {
            Map<Integer, ItemStack> leftover = inv.removeItem(item);
            if (!leftover.isEmpty()) {
                Bukkit.getLogger().warning("Removing items from " + player.getName() + "'s inventory gave a leftover; allowing payment anyways.");
            }

            player.updateInventory();

            String message = DragonTravelMain.getInstance().getMessagesHandler().getMessage("Messages.Payment.Resources.Successful.WithdrawMessage");
            message = message.replace("{amount}", "%d");
            message = String.format(message, amount);
            player.sendMessage(message);
            return true;
        } else {
            player.sendMessage(DragonTravelMain.getInstance().getMessagesHandler().getMessage("Messages.Payment.Resources.Error.NotEnoughResources"));
            return false;
        }
    }
}