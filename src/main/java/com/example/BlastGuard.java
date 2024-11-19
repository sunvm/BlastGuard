package com.example;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.EventPriority;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockIterator;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class BlastGuard extends JavaPlugin implements Listener {

    // Радиус защиты от взрывов
    private int blastGuardRadius;
    // Хранилище активных тотемов (их локаций)
    private Set<Location> activeTotems;

    /**
     * Вызывается при включении плагина
     */
    @Override
    public void onEnable() {
        // Инициализация конфигурации
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        blastGuardRadius = config.getInt("blast-guard-radius", 5);
        
        // Инициализация коллекции активных тотемов
        activeTotems = new HashSet<>();
        
        // Загрузка сохраненных тотемов
        loadTotems();
        
        // Регистрация обработчиков событий
        Bukkit.getPluginManager().registerEvents(this, this);
        
        getLogger().info("=== BlastGuard has been enabled! ===");
        getLogger().info("Protection radius: " + blastGuardRadius);
        getLogger().info("Active totems loaded: " + activeTotems.size());
    }

    /**
     * Вызывается при выключении плагина
     */
    @Override
    public void onDisable() {
        // Сохранение тотемов перед выключением
        saveTotems();
        getLogger().info("=== BlastGuard has been disabled! ===");
        getLogger().info("Active totems saved: " + activeTotems.size());
    }

    // Метод для сохранения тотемов
    private void saveTotems() {
        List<String> totemLocations = new ArrayList<>();
        for (Location loc : activeTotems) {
            totemLocations.add(String.format("%s,%d,%d,%d",
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()));
        }
        getConfig().set("totems", totemLocations);
        saveConfig();
        getLogger().info("Saved " + totemLocations.size() + " totems");
    }

    // Метод для загрузки тотемов
    private void loadTotems() {
        List<String> totemLocations = getConfig().getStringList("totems");
        for (String locationStr : totemLocations) {
            try {
                String[] parts = locationStr.split(",");
                Location loc = new Location(
                    Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
                activeTotems.add(loc);
                getLogger().info("Loaded totem at: " + loc);
            } catch (Exception e) {
                getLogger().warning("Failed to load totem location: " + locationStr);
            }
        }
    }

    /**
     * Обработчик события взрыва
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        
        if (activeTotems == null || activeTotems.isEmpty()) {
            getLogger().info("No active totems to protect from explosion");
            return;
        }

        Location explosionLocation = event.getLocation();
        getLogger().info("Explosion detected at: " + explosionLocation);
        
        for (Location totemLocation : activeTotems) {
            if (totemLocation.getWorld().equals(explosionLocation.getWorld()) &&
                totemLocation.distance(explosionLocation) <= blastGuardRadius) {
                event.setCancelled(true);
                getLogger().info("Explosion cancelled by totem at: " + totemLocation);
                // Сохраняем состояние после каждого успешного блокирования
                saveTotems();
                return;
            }
        }
    }

    /**
     * Обработчик разрушения блоков тотема
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        getLogger().info("Block broken at: " + blockLocation);
        
        // Проверяем, является ли разрушенный блок частью тотема
        if (activeTotems.contains(blockLocation)) {
            activeTotems.remove(blockLocation);
            blockLocation.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
            event.getPlayer().sendMessage("§cТотем разрушен!");
            getLogger().info("Totem destroyed at: " + blockLocation);
            saveTotems(); // Сохраняем изменения
        } else if (activeTotems.contains(blockLocation.clone().add(0, -1, 0))) {
            Location totemLocation = blockLocation.clone().add(0, -1, 0);
            activeTotems.remove(totemLocation);
            event.getBlock().setType(Material.AIR);
            event.getPlayer().sendMessage("§cТотем разрушен!");
            getLogger().info("Totem destroyed at: " + totemLocation);
            saveTotems(); // Сохраняем изменения
        }
    }

    /**
     * Обработчик команды выдачи тотема
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("totem")) {
            if (args.length == 0) {
                sender.sendMessage("§cИспользование: /totem <give|place> [player]");
                return true;
            }

            // Команда выдачи тотема
            if (args[0].equalsIgnoreCase("give")) {
                // Проверяем права
                if (!sender.isOp()) {
                    sender.sendMessage("§cУ вас нет прав для использования этой команды!");
                    return true;
                }

                Player targetPlayer;

                // Если указано имя игрока
                if (args.length > 1) {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null) {
                        sender.sendMessage("§cИгрок " + args[1] + " не найден!");
                        return true;
                    }
                } else if (sender instanceof Player) {
                    // Если имя не указано, но команду выполняет игрок
                    targetPlayer = (Player) sender;
                } else {
                    // Если команда из консоли без указания игрока
                    sender.sendMessage("§cУкажите имя игрока!");
                    return true;
                }

                // Создаем тотем
                ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);
                ItemMeta meta = totem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("BLAST GUARD");
                    totem.setItemMeta(meta);
                    getLogger().info("Created BLAST GUARD totem for player: " + targetPlayer.getName());
                }

                // Выдаем тотем игроку
                targetPlayer.getInventory().addItem(totem);
                targetPlayer.sendMessage("§aВы получили тотем защиты от взрывов!");
                if (sender != targetPlayer) {
                    sender.sendMessage("§aТотем выдан игроку " + targetPlayer.getName());
                }
                return true;
            }

            // Команда установки тотема
            if (args[0].equalsIgnoreCase("place")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cЭта команда доступна только для игроков!");
                    return true;
                }

                Player player = (Player) sender;
                Block targetBlock = player.getTargetBlock(null, 5);
                
                if (targetBlock != null && targetBlock.getType() != Material.AIR) {
                    getLogger().info("Target block found at: " + targetBlock.getLocation());

                    try {
                        Location placeLoc = targetBlock.getLocation().add(0, 1, 0);
                        Block lowerBlock = placeLoc.getBlock();
                        Block upperBlock = placeLoc.clone().add(0, 1, 0).getBlock();

                        if (lowerBlock.getType() != Material.AIR || upperBlock.getType() != Material.AIR) {
                            player.sendMessage("§cНевозможно установить тотем - место занято!");
                            return true;
                        }

                        ItemStack itemInHand = player.getInventory().getItemInMainHand();
                        if (itemInHand.getType() != Material.TOTEM_OF_UNDYING) {
                            player.sendMessage("§cВы должны держать тотем в руке!");
                            return true;
                        }

                        ItemMeta meta = itemInHand.getItemMeta();
                        if (meta == null || !"BLAST GUARD".equals(meta.getDisplayName())) {
                            player.sendMessage("§cВы должны использовать специальный тотем BLAST GUARD!");
                            return true;
                        }

                        // Устанавливаем структуру тотема
                        lowerBlock.setType(Material.QUARTZ_BLOCK);
                        upperBlock.setType(Material.EMERALD_BLOCK);

                        // Проверяем, что коллекция инициализирована
                        if (activeTotems == null) {
                            activeTotems = new HashSet<>();
                            getLogger().warning("ActiveTotems collection was null and has been reinitialized");
                        }

                        // Регистрируем тотем
                        activeTotems.add(lowerBlock.getLocation());
                        saveTotems(); // Сохраняем новый тотем
                        getLogger().info("Added totem at: " + lowerBlock.getLocation());
                        getLogger().info("Current active totems: " + activeTotems.size());

                        // Уменьшаем количество тотемов в руке
                        int amount = itemInHand.getAmount();
                        if (amount > 1) {
                            itemInHand.setAmount(amount - 1);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                        }

                        player.sendMessage("§aТотем успешно установлен!");
                        getLogger().info("Totem placed successfully at: " + lowerBlock.getLocation());
                    } catch (Exception e) {
                        getLogger().severe("Error placing totem: " + e.getMessage());
                        e.printStackTrace();
                        player.sendMessage("§cОшибка при установке тотема!");
                    }
                } else {
                    player.sendMessage("§cНет подходящего блока для установки тотема!");
                }
                return true;
            }
        }
        return false;
    }
} 