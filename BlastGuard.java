import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public class BlastGuard extends JavaPlugin implements Listener {

    // Радиус действия защиты от взрывов
    private int blastGuardRadius;
    // Набор активных тотемов, которые защищают от взрывов
    private Set<Location> activeTotems = new HashSet<>();

    @Override
    public void onEnable() {
        // Загрузка конфигурации и установка радиуса действия
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        blastGuardRadius = config.getInt("blast-guard-radius");
        // Регистрация событий
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Получение местоположения взрыва
        Location explosionLocation = event.getLocation();
        // Проверка, находится ли взрыв в радиусе действия любого активного тотема
        for (Location totemLocation : activeTotems) {
            if (totemLocation.distance(explosionLocation) <= blastGuardRadius) {
                // Отмена взрыва, если он в радиусе действия
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        // Проверка, держит ли игрок "Тотем бессмертия" с именем "BLAST GUARD"
        if (itemInHand.getType() == Material.TOTEM_OF_UNDYING) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null && "BLAST GUARD".equals(meta.getDisplayName())) {
                if (event.getClickedBlock() != null) { // Проверка на null
                    Location clickedLocation = event.getClickedBlock().getLocation().add(0, 1, 0);
                    Block lowerBlock = clickedLocation.getBlock();
                    Block upperBlock = clickedLocation.clone().add(0, 1, 0).getBlock();

                    // Создание структуры из кварцевого и изумрудного блоков
                    lowerBlock.setType(Material.QUARTZ_BLOCK);
                    upperBlock.setType(Material.EMERALD_BLOCK);

                    // Добавление местоположения тотема в активные
                    activeTotems.add(lowerBlock.getLocation());
                    // Удаление тотема из инвентаря игрока
                    player.getInventory().getItemInMainHand().setAmount(0);
                    player.sendMessage("Totem placed!");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        // Проверка, является ли разрушенный блок частью активного тотема
        if (activeTotems.contains(blockLocation) || activeTotems.contains(blockLocation.clone().add(0, -1, 0))) {
            // Удаление тотема из активных, если один из блоков разрушен
            activeTotems.remove(blockLocation);
            activeTotems.remove(blockLocation.clone().add(0, -1, 0));
            event.getPlayer().sendMessage("Totem destroyed!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("giveTotem")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // Проверка, является ли игрок оператором
                if (!player.isOp()) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                // Создание и переименование тотема
                ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);
                ItemMeta meta = totem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("BLAST GUARD");
                    totem.setItemMeta(meta);
                }
                // Добавление тотема в инвентарь игрока
                player.getInventory().addItem(totem);
                player.sendMessage("You have received a Totem of Undying!");
                return true;
            }
        }
        return false;
    }
}
