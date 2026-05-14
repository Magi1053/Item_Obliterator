package elocindev.item_obliterator.neoforge.config;

import java.util.ArrayList;
import java.util.List;

import elocindev.necronomicon.api.config.v1.NecConfigAPI;
import elocindev.necronomicon.config.Comment;
import elocindev.necronomicon.config.NecConfig;

public class ConfigEntries {
	@NecConfig
	public static ConfigEntries INSTANCE;
	
	public static String getFile() {
		return NecConfigAPI.getFile("item_obliterator.json5");
	}
	@Comment("-----------------------------------------------------------")
	@Comment("             Item Obliterator by ElocinDev")
	@Comment("-----------------------------------------------------------")
	@Comment(" ")
	@Comment("How to add items?")
	@Comment("  - They are json strings, so you need to separate each")
	@Comment("    entry with a comma, except the last")
	@Comment("  - If you start an entry with !, it will be treated as a regular expression")
	@Comment("    Example: \"!minecraft:.*_sword\" to disable all swords")
	@Comment(" ")
	@Comment("-----------------------------------------------------------")
	@Comment("Do not touch this")
	public int configVersion = 2;

	@Comment("-----------------------------------------------------------")

	@Comment("Items here will be unusable completely")
	@Comment("   Example: minecraft:diamond")
	public List<String> blacklisted_items = new ArrayList<>() {{
		add("examplemod:example_item");
	}};

	@Comment("-----------------------------------------------------------")
	@Comment("Removes an item if it contains certain nbt tag. If the whole entry (or expression) is present, the item gets removed.")
	@Comment("Use with caution! This is a very expensive operation and can cause lag if you have a lot of items blacklisted.")
	@Comment("	")
	@Comment("	 On Minecraft 1.21+, item data often lives in data components (not only minecraft:custom_data).")
	@Comment("	 When blacklisted_nbt_match_stack_snbt is true, each pattern is also matched against the full stack SNBT")
	@Comment("	 produced by ItemStack#save (same representation as /give and creative tooltips), so substrings like")
	@Comment("	 template_id:\"ender_hook\" or mod component keys in bracket form can match.")
	@Comment("	")
	@Comment("	 Example to disable a regeneration potion: Potion:\"minecraft:regeneration\"")
	@Comment("	")
	@Comment("	 You can also use regular expressions by starting the value with !")
	public List<String> blacklisted_nbt = new ArrayList<>() {{}};

	@Comment("-----------------------------------------------------------")
	@Comment("If true, each blacklisted_nbt entry is matched against the full item stack SNBT (components + count), not only CustomData.")
	@Comment("Disable for legacy packs that relied on matching only the inner custom_data tag string, or if you need slightly less work per check.")
	public boolean blacklisted_nbt_match_stack_snbt = true;

	@Comment("-----------------------------------------------------------")

	@Comment("Items here will not be able to be right-clicked (Interact)")
	@Comment("   Example: minecraft:apple")
  	public List<String> only_disable_interactions = new ArrayList<>() {{
		add("examplemod:example_item");
    }};

	@Comment("-----------------------------------------------------------")

	@Comment("Items here will not be able to be used to attack")
	@Comment("   Example: minecraft:diamond_sword")
	public List<String> only_disable_attacks = new ArrayList<>() {{
		add("examplemod:example_item");
	}};

	@Comment("-----------------------------------------------------------")

	@Comment("Items here will get their recipes disabled")
	@Comment("Keep in mind this already is applied to blacklisted items")
	public List<String> only_disable_recipes = new ArrayList<>() {{
		add("examplemod:example_item");
	}};

	@Comment("-----------------------------------------------------------")

	@Comment("If true, the mod will use a hashset to handle the blacklisted items")
	@Comment("This is a more optimized approach only if you have a lot of items blacklisted (20 or more is recommended)")
	@Comment("If you just have a small amount of items blacklisted, keep this false")
	@Comment(" ")
	@Comment("[!] Enabling this will disable all regular expressions")
	@Comment("[!] Does not apply to NBT, only item blacklist / interaction / attack")
	public boolean use_hashmap_optimizations = false;
}