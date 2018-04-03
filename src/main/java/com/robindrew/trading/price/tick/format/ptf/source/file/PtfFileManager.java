package com.robindrew.trading.price.tick.format.ptf.source.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.robindrew.common.io.Files;
import com.robindrew.common.util.Check;
import com.robindrew.trading.IInstrument;
import com.robindrew.trading.Instrument;
import com.robindrew.trading.InstrumentType;
import com.robindrew.trading.price.tick.format.ptf.source.IPtfSourceManager;
import com.robindrew.trading.price.tick.format.ptf.source.IPtfSourceSet;
import com.robindrew.trading.provider.ITradeDataProvider;
import com.robindrew.trading.provider.TradeDataProvider;

public class PtfFileManager implements IPtfSourceManager {

	private static final Logger log = LoggerFactory.getLogger(PtfFileManager.class);

	public static File getDirectory(ITradeDataProvider provider, IInstrument instrument, File rootDirectory) {
		File providerDir = new File(rootDirectory, provider.name());
		File typeDir = new File(providerDir, instrument.getType().name());
		return new File(typeDir, instrument.getName());
	}

	private final File rootDirectory;
	private final Map<IInstrument, IPtfFileSet> instrumentMap = new ConcurrentHashMap<>();
	private final Set<ITradeDataProvider> providers;

	public PtfFileManager(File directory) {
		this(directory, Collections.emptySet());
	}

	public PtfFileManager(File directory, ITradeDataProvider... providers) {
		this(directory, new LinkedHashSet<>(Arrays.asList(providers)));
	}

	public PtfFileManager(File directory, Set<? extends ITradeDataProvider> providers) {
		this.rootDirectory = Check.notNull("directory", directory);
		this.providers = ImmutableSet.copyOf(providers);

		for (ITradeDataProvider provider : providers) {
			File providerDir = new File(rootDirectory, provider.name());
			if (!providerDir.exists()) {
				continue;
			}

			log.info("[Provider] {}", provider);
			for (File typeDir : Files.listFiles(providerDir, false)) {
				InstrumentType type = InstrumentType.valueOf(typeDir.getName());

				for (File instrumentDir : Files.listFiles(typeDir, false)) {
					IInstrument instrument = new Instrument(instrumentDir.getName(), type);

					log.info("[Instrument] {}", instrument);
				}
			}
		}
	}

	@Override
	public Set<IInstrument> getInstruments() {
		Set<IInstrument> set = new TreeSet<>();
		for (ITradeDataProvider provider : providers) {
			File providerDir = new File(rootDirectory, provider.name());
			if (!providerDir.exists()) {
				continue;
			}
			for (File typeDir : Files.listFiles(providerDir, false)) {
				InstrumentType type = InstrumentType.valueOf(typeDir.getName());
				for (File instrumentDir : Files.listFiles(typeDir, false)) {
					IInstrument instrument = new Instrument(instrumentDir.getName(), type);
					set.add(instrument);
				}
			}
		}
		return set;
	}

	@Override
	public IPtfFileSet getSourceSet(IInstrument instrument) {
		IPtfFileSet set = instrumentMap.get(instrument);
		if (set == null) {
			instrumentMap.putIfAbsent(instrument, new PtfFileSet(instrument, rootDirectory, providers));
			set = instrumentMap.get(instrument);
		}
		return set;
	}

	@Override
	public Set<ITradeDataProvider> getProviders() {
		Set<ITradeDataProvider> set = new LinkedHashSet<>();
		if (providers.isEmpty()) {
			for (File providerDir : Files.listFiles(rootDirectory, false)) {
				if (providerDir.isDirectory()) {
					String name = providerDir.getName();
					TradeDataProvider provider = TradeDataProvider.valueOf(name);
					set.add(provider);
				}
			}
		} else {
			for (ITradeDataProvider provider : providers) {
				File providerDir = new File(rootDirectory, provider.name());
				if (providerDir.exists()) {
					set.add(provider);
				}
			}
		}
		return set;
	}

	@Override
	public IPtfSourceSet getSourceSet(IInstrument instrument, ITradeDataProvider provider) {
		return new PtfFileSet(instrument, rootDirectory, ImmutableSet.of(provider));
	}

	@Override
	public Set<IInstrument> getInstruments(ITradeDataProvider provider) {
		Set<IInstrument> set = new TreeSet<>();
		File providerDir = new File(rootDirectory, provider.name());
		if (providerDir.exists()) {
			for (File typeDir : Files.listFiles(providerDir, false)) {
				InstrumentType type = InstrumentType.valueOf(typeDir.getName());
				for (File instrumentDir : Files.listFiles(typeDir, false)) {
					IInstrument instrument = new Instrument(instrumentDir.getName(), type);
					set.add(instrument);
				}
			}
		}
		return set;
	}

}