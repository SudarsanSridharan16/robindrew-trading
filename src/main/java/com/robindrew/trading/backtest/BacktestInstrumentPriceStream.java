package com.robindrew.trading.backtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.trading.platform.streaming.InstrumentPriceStream;
import com.robindrew.trading.price.candle.IPriceCandle;
import com.robindrew.trading.price.candle.io.stream.source.IPriceCandleStreamSource;
import com.robindrew.trading.price.history.IInstrumentPriceHistory;

public class BacktestInstrumentPriceStream extends InstrumentPriceStream implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(BacktestInstrumentPriceStream.class);

	private final IInstrumentPriceHistory history;

	public BacktestInstrumentPriceStream(IInstrumentPriceHistory history) {
		super(history.getInstrument());
		this.history = history;
	}

	@Override
	public void run() {
		IPriceCandleStreamSource source = history.getStreamSource();
		log.info("[Started Streaming Prices] {}", getInstrument());
		while (true) {
			IPriceCandle candle = source.getNextCandle();
			if (candle == null) {
				break;
			}
			putNextCandle(candle);
		}
		log.info("[Finished Streaming Prices] {}", getInstrument());
	}

	@Override
	public String getName() {
		return "BacktestInstrumentPriceStreamListener[" + getInstrument() + "]";
	}

	@Override
	public void close() {
	}

}
