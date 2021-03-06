package com.robindrew.trading.price.candle.format.pcf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.robindrew.common.io.data.IDataReader;
import com.robindrew.common.io.data.IDataSerializer;
import com.robindrew.common.io.data.IDataWriter;
import com.robindrew.trading.price.candle.IPriceCandle;
import com.robindrew.trading.price.candle.MidPriceCandle;

public class PcfDataSerializer implements IDataSerializer<List<IPriceCandle>> {

	@Override
	public List<IPriceCandle> readObject(IDataReader reader) throws IOException {
		List<IPriceCandle> list = new ArrayList<>();

		int count = reader.readPositiveInt();
		int basePrice = reader.readPositiveInt();
		long baseTime = reader.readPositiveLong();
		int decimalPlaces = reader.readPositiveInt();

		for (int i = 0; i < count; i++) {

			long openTime = reader.readPositiveLong();
			long closeTime = reader.readPositiveLong();

			openTime += baseTime;
			closeTime += openTime;

			int open = reader.readDynamicInt() + basePrice;
			int high = reader.readDynamicInt() + basePrice;
			int low = reader.readDynamicInt() + basePrice;
			int close = reader.readDynamicInt() + basePrice;

			long tickVolume = reader.readPositiveLong();

			IPriceCandle candle = new MidPriceCandle(open, high, low, close, openTime, closeTime, decimalPlaces, tickVolume);
			list.add(candle);

			basePrice = close;
			baseTime = closeTime;
		}

		return list;
	}

	@Override
	public void writeObject(IDataWriter writer, List<IPriceCandle> candles) throws IOException {
		if (candles.isEmpty()) {
			throw new IllegalArgumentException("candles is empty");
		}

		IPriceCandle firstCandle = candles.get(0);

		int count = candles.size();
		int basePrice = firstCandle.getMidOpenPrice();
		long baseTime = firstCandle.getOpenTime();
		int decimalPlaces = firstCandle.getDecimalPlaces();

		writer.writePositiveInt(count);
		writer.writePositiveInt(basePrice);
		writer.writePositiveLong(baseTime);
		writer.writePositiveInt(decimalPlaces);

		IPriceCandle previous = null;
		for (IPriceCandle candle : candles) {
			try {

				long openTime = candle.getOpenTime();
				long closeTime = candle.getCloseTime();

				int open = candle.getMidOpenPrice();
				int high = candle.getMidHighPrice();
				int low = candle.getMidLowPrice();
				int close = candle.getMidClosePrice();

				writer.writePositiveLong(openTime - baseTime);
				writer.writePositiveLong(closeTime - openTime);

				writer.writeDynamicInt(open - basePrice);
				writer.writeDynamicInt(high - basePrice);
				writer.writeDynamicInt(low - basePrice);
				writer.writeDynamicInt(close - basePrice);

				writer.writePositiveLong(candle.getTickVolume());

				basePrice = close;
				baseTime = closeTime;

				previous = candle;
			} catch (Exception e) {
				throw new IOException("Failed to serialize candle: " + candle + " (previous=" + previous + ")", e);
			}
		}
	}

}
