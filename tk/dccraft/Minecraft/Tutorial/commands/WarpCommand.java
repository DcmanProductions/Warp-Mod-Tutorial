package tk.dccraft.Minecraft.Tutorial.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import tk.dccraft.Minecraft.Tutorial.commands.util.Teleport;

public class WarpCommand implements ICommand {

	private static Map<String, Entry<BlockPos, Integer>> warps = new HashMap<>();
	private static List<String> aliases = Arrays.asList("tpx", "tut_warp_mod");
	private EntityPlayer player;
	private BufferedWriter bw;
	private BufferedReader br;
	private String fileName = "warps.conf", fileLocation = "config/Warps/";

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			setPlayer((EntityPlayer) sender);
			if (!isRemote()) {

				getWarps();

				if (args[0].equalsIgnoreCase("random")) {
					if (args.length == 2) {
						int range = 0;
						try {
							range = Integer.parseInt(args[1]);
						} catch (NumberFormatException e) {
							sendMessage(TextFormatting.RED + "Ex:/warp random 200");
						} catch (Exception e) {
							sendMessage(TextFormatting.RED + "Something Went Wrong");
							e.printStackTrace();
						}
						warpRandom(range);
					} else {
						warpRandom(600);
					}
				}

				if (args[0].equalsIgnoreCase("list")) {
					// for (String s : getWarps()) {
					// sendMessage(TextFormatting.GOLD + s);
					// }
					sendMessage(TextFormatting.GOLD + Arrays.toString(getWarps().toArray()).replace("[", "").replace("]", "").replace(",", ""));
				}

				if (args[0].equalsIgnoreCase("me")) {
					if (args.length == 2) {
						if (server.getPlayerList().getPlayers().size() > 1) {
							for (EntityPlayer p : server.getPlayerList().getPlayers()) {
								if (args[1].equalsIgnoreCase(p.getDisplayNameString())) {
									double x, y, z;
									x = p.getPosition().getX();
									y = p.getPosition().getY();
									z = p.getPosition().getZ();
									Teleport.teleport(getPlayer(), p.getEntityWorld().provider.getDimension(), x, y, z);
								}
							}
						} else {
							sendMessage(TextFormatting.RED + "Huh.... You Know You're all alone right?");
						}
					}
				}
				if (args.length > 1 && args[1].equalsIgnoreCase("me")) {
					if (server.getPlayerList().getPlayers().size() > 1) {
						for (EntityPlayer p : server.getPlayerList().getPlayers()) {
							if (args[0].equalsIgnoreCase(p.getDisplayNameString())) {
								double x, y, z;
								x = getPlayer().getPosition().getX();
								y = getPlayer().getPosition().getY();
								z = getPlayer().getPosition().getZ();
								Teleport.teleport(p, getPlayer().getEntityWorld().provider.getDimension(), x, y, z);
							}
						}
					}
				}

				if (args[0].equalsIgnoreCase("remove")) {
					if (args.length == 2) {
						for (String s : getWarps()) {
							if (args[1].equalsIgnoreCase(s)) {
								removeWarp(args[1]);
							}
						}
					}
				}

				if (args[0].equalsIgnoreCase("map")) {
					if (warps == null)
						getWarps();
					if (getWarps() == null) {
						sendMessage(TextFormatting.RED + "No Warps Found");
						return;
					}

					if (args.length == 2) {
						for (String name : warps.keySet()) {
							if (args[1].equalsIgnoreCase(name)) {
								BlockPos pos = warps.get(name).getKey();
								int dim = warps.get(name).getValue();
								String dimesion = DimensionManager.getProvider(dim).getDimensionType().getName() + "(" + dim + ")";
								sendMessage(TextFormatting.GOLD + name + ":" + TextFormatting.RED + "{" + TextFormatting.GREEN + " X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", World:" + dimesion + TextFormatting.RED + "}");
								return;
							}
						}
					}

					for (String name : warps.keySet()) {
						BlockPos pos = warps.get(name).getKey();
						int dim = warps.get(name).getValue();
						String dimesion = DimensionManager.getProvider(dim).getDimensionType().getName() + "(" + dim + ")";
					}

				}

				if (args[0].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("back")) {
						sendMessage(TextFormatting.RED + "Back is already a pre-allocated warp");
						return;
					}
					createWarp(args[1].toLowerCase());
				}

				if (args.length == 1 && !(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("random"))) {
					warpTo(args[0]);
				}

			} else {
				System.out.println("World Is Remote");
			}
		} else {
			System.out.println("Sender isn't a Player");
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return null;
	}

	private void removeWarp(String name) {
		warps.remove(name);
		sendMessage(TextFormatting.GOLD + "Warp Removed: " + name);
		export();
	}

	private List<String> getOnlinePlayers() {
		List<String> names = new ArrayList<>();
		for (EntityPlayer player : getPlayer().getServer().getPlayerList().getPlayers()) {
			if (!player.equals(getPlayer()))
				names.add(player.getDisplayNameString());
		}
		return names;
	}

	private void warpRandom(int range) {
		Random ran = new Random();
		int var = ran.nextInt(range);
		double x, y, z;
		x = getPlayer().getPosition().getX() + var;
		y = 255;
		z = getPlayer().getPosition().getZ() + var;

		BlockPos pos = new BlockPos(x, y, z);
		while (getPlayer().world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
			y--;
			pos = new BlockPos(x, y, z);
		}
		y += 2;
		back(getPlayer().getPosition(), getPlayer().getEntityWorld().provider.getDimension());
		Teleport.teleport(getPlayer(), getPlayer().getEntityWorld().provider.getDimension(), x, y, z);
		sendMessage(TextFormatting.AQUA + "Warping " + var + " blocks away");
	}

	private void warpTo(String name, EntityPlayer... entityPlayers) {
		// if (entityPlayers == null) {
		EntityPlayerMP player = (EntityPlayerMP) getPlayer();
		if (warps.containsKey(name)) {
			double x, y, z;
			x = (double) warps.get(name).getKey().getX();
			y = (double) warps.get(name).getKey().getY();
			z = (double) warps.get(name).getKey().getZ();
			BlockPos oldPos = player.getPosition();
			int oldDim = player.getEntityWorld().provider.getDimension();
			int dimension = warps.get(name).getValue();
			Teleport.teleport(player, dimension, x, y, z);
			back(oldPos, oldDim);
		} else {
			sendMessage(TextFormatting.RED + "Warp Not Found: " + name.toUpperCase());
		}
		// }
	}

	private void back(BlockPos pos, int dimension) {
		this.warps.put("back", new AbstractMap.SimpleEntry(pos, dimension));
		sendMessage(TextFormatting.GREEN + "Back Warp Save: type " + TextFormatting.GOLD + "\"/warp back\"" + TextFormatting.GREEN + " to warp back");
		export();
	}

	private List<String> getWarps() {
		List<String> warps = new ArrayList<>();
		FileReader file;
		fileName = getPlayer().getDisplayNameString() + "_" + getPlayer().getServer().getFolderName() + ".conf";
		try {
			file = new FileReader(fileLocation + fileName);
			br = new BufferedReader(file);
		} catch (FileNotFoundException e) {
			try {
				TextWriter("");
				return Arrays.asList("No Warps Saved");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String line = br.readLine();

			while (line != null) {
				if (line.isEmpty()) {
					line = br.readLine();
				}
				try {
					String[] text = line.split(":");
					double x, y, z;
					x = Double.parseDouble(text[2].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", ""));
					y = Double.parseDouble(text[3].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", ""));
					z = Double.parseDouble(text[4].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", ""));
					int dimension = Integer.parseInt(text[5].replaceAll("World", ""));
					BlockPos pos = new BlockPos(x, y, z);
					this.warps.put(text[0].replace(":", ""), new AbstractMap.SimpleEntry(pos, dimension));
					warps.add(text[0]);
				} catch (NullPointerException e) {
					e.printStackTrace();
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				line = br.readLine();
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return warps;
	}

	private void createWarp(String name, EntityPlayer... playerIn) {
		// if (playerIn == null) {
		if (playerIn != null) {
			System.out.println("Huh this doesn't equal null");
		}
		EntityPlayerMP player = (EntityPlayerMP) getPlayer();
		double x = player.getPosition().getX(), y = player.getPosition().getY(), z = player.getPosition().getZ();
		int dimension = player.getEntityWorld().provider.getDimension();
		BlockPos pos = new BlockPos(x, y, z);

		if (warps.get(name) != null) {
			warps.replace(name, new AbstractMap.SimpleEntry(pos, dimension));
			sendMessage(TextFormatting.GREEN + "Warp Overriden: " + TextFormatting.GOLD + name);
		} else {
			warps.put(name, new AbstractMap.SimpleEntry(pos, dimension));
			sendMessage(TextFormatting.GREEN + "Warp Created: " + TextFormatting.GOLD + name);
		}

		export();
		// }
	}

	public void export() {
		int index = 0;
		String[] warpString = new String[warps.size()];
		for (String name : warps.keySet()) {
			int x = warps.get(name).getKey().getX(), y = warps.get(name).getKey().getY(), z = warps.get(name).getKey().getZ();
			int dimension = warps.get(name).getValue();
			warpString[index] = name + ":" + " X:" + x + " Y:" + y + " Z:" + z + " World:" + dimension;
			index++;
		}
		try {
			TextWriter(Arrays.toString(warpString).replace("[", "").replace("]", "").replace(", ", "\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void TextWriter(String text, EntityPlayer... playerIn) throws IOException {
		// if (playerIn == null) {
		File f = new File(fileLocation);
		try {
			if (f.mkdirs()) {
				System.out.println("Warp File Created in " + f.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileName = getPlayer().getDisplayNameString() + "_" + getPlayer().getServer().getFolderName() + ".conf";
		try {
			bw = new BufferedWriter(new FileWriter(fileLocation + fileName, false));
			if (!text.isEmpty())
				bw.write(text);
			bw.flush();
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		// }
	}

	public void sendMessage(Object message) {
		getPlayer().sendMessage(new TextComponentString(message + ""));
	}

	public void setPlayer(EntityPlayer value) {
		this.player = value;
	}

	public EntityPlayer getPlayer() {
		if (player != null)
			return this.player;
		return null;
	}

	public boolean isRemote() {
		return getPlayer().getEntityWorld().isRemote;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand arg0) {
		return 0;
	}

	@Override
	public String getName() {
		return "warp";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Type \"/warp help\" to get more info";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

}
