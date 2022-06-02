package com.elikill58.negativity.universal.dataStorage.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.api.yaml.Configuration;
import com.elikill58.negativity.api.yaml.YamlConfiguration;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.Minerate;
import com.elikill58.negativity.universal.TranslatedMessages;
import com.elikill58.negativity.universal.account.NegativityAccount;
import com.elikill58.negativity.universal.dataStorage.NegativityAccountStorage;
import com.elikill58.negativity.universal.report.Report;

public class FileNegativityAccountStorage extends NegativityAccountStorage {

	private final File userDir;

	public FileNegativityAccountStorage(File userDir) {
		this.userDir = userDir;
	}

	@Override
	public CompletableFuture<@Nullable NegativityAccount> loadAccount(UUID playerId) {
		return CompletableFuture.supplyAsync(() -> {
			File file = new File(userDir, playerId + ".yml");
			if (!file.exists()) {
				return null;
			}
			try {
				Configuration config = YamlConfiguration.load(file);
				String playerName = config.getString("playername");
				String language = config.getString("lang", TranslatedMessages.getDefaultLang());
				Minerate minerate = deserializeMinerate(config.getInt("minerate-full-mined"), config.getSection("minerate"));
				int mostClicksPerSecond = config.getInt("better-click");
				Map<String, Long> warns = deserializeViolations(config.getSection("cheats"));
				List<Report> reports = deserializeReports(config);
				String IP = config.getString("ip", "0.0.0.0");
				long creationTime = config.getLong("creation-time", System.currentTimeMillis());
				boolean showAlert = config.getBoolean("show-alert", true);
				return new NegativityAccount(playerId, playerName, language, minerate, mostClicksPerSecond, warns, reports, IP, creationTime, showAlert);
			} catch (Exception e) { // prevent parsing error due to corrupted file.
				Adapter ada = Adapter.getAdapter();
				ada.getLogger().info("File account of " + ada.getOfflinePlayer(playerId).getName() + " have been corrupted. Creating a new one ...");
				if(!file.delete())
					file.deleteOnExit();
				NegativityAccount acc = new NegativityAccount(playerId);
				// TODO try to get most data as possible from old file
				saveAccount(acc).join();
				return acc;
			}
		});
	}

	@Override
	public CompletableFuture<Void> saveAccount(NegativityAccount account) {
		return CompletableFuture.runAsync(() -> {
			File file = new File(userDir, account.getPlayerId() + ".yml");
			if(!file.exists()) {
				try {
					userDir.mkdirs();
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Configuration accountConfig = YamlConfiguration.load(file);
			accountConfig.set("playername", account.getPlayerName());
			accountConfig.set("lang", account.getLang());
			accountConfig.set("minerate-full-mined", account.getMinerate().getFullMined());
			serializeMinerate(account.getMinerate(), accountConfig.createSection("minerate"));
			accountConfig.set("better-click", account.getMostClicksPerSecond());
			serializeViolations(account, accountConfig.createSection("cheats"));
			serializeReports(account, accountConfig);
			accountConfig.set("ip", account.getIp());
			accountConfig.set("creation-time", account.getCreationTime());
			accountConfig.save();
		});
	}

	private void serializeMinerate(Minerate minerate, Configuration minerateSection) {
		for (Minerate.MinerateType minerateType : Minerate.MinerateType.values()) {
			String key = minerateType.getName().toLowerCase(Locale.ROOT);
			minerateSection.set(key, minerate.getMinerateType(minerateType));
		}
	}

	private Minerate deserializeMinerate(int fullMined, @Nullable Configuration minerateSection) {
		HashMap<Minerate.MinerateType, Integer> mined = new HashMap<>();
		if (minerateSection == null) {
			return new Minerate(mined, fullMined);
		}

		for (String minerateKey : minerateSection.getKeys()) {
			Minerate.MinerateType type = Minerate.MinerateType.getMinerateType(minerateKey);
			if (type == null) {
				continue;
			}
			mined.put(type, minerateSection.getInt(minerateKey));
		}

		return new Minerate(mined, fullMined);
	}

	private void serializeViolations(NegativityAccount account, Configuration cheatsSection) {
		for (Map.Entry<String, Long> entry : account.getAllWarns().entrySet()) {
			String cheatKey = entry.getKey().toLowerCase(Locale.ROOT);
			cheatsSection.set(cheatKey, entry.getValue());
		}
	}

	private Map<String, Long> deserializeViolations(@Nullable Configuration cheatsSection) {
		Map<String, Long> violations = new HashMap<>();
		if (cheatsSection == null) {
			return violations;
		}

		for (String cheatKey : cheatsSection.getKeys()) {
			violations.put(cheatKey, cheatsSection.getLong(cheatKey));
		}
		return violations;
	}

	private void serializeReports(NegativityAccount account, Configuration section) {
		List<String> list = new ArrayList<>();
		account.getReports().forEach((r) -> {
			list.add(r.toJsonString());
		});
		section.set("reports", list);
	}

	private List<Report> deserializeReports(@Nullable Configuration cheatsSection) {
		List<Report> reports = new ArrayList<>();
		if (cheatsSection == null) {
			return reports;
		}

		for (String fullEntry : cheatsSection.getStringList("reports")) {
			Report report = Report.fromJson(fullEntry);
			if(report != null)
				reports.add(report);
		}
		return reports;
	}
	
	@Override
	public List<UUID> getPlayersOnIP(String ip) {
		// TODO Implement getting players on IP when using file system
		return Collections.emptyList();
	}
}
