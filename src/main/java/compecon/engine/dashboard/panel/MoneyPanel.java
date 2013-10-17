/*
Copyright (C) 2013 u.wol@wwu.de 
 
This file is part of ComputationalEconomy.

ComputationalEconomy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ComputationalEconomy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ComputationalEconomy. If not, see <http://www.gnu.org/licenses/>.
 */

package compecon.engine.dashboard.panel;

import java.awt.GridLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.Simulation;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;

public class MoneyPanel extends AbstractChartsPanel implements IModelListener {

	public MoneyPanel() {
		this.setLayout(new GridLayout(0, 2));

		this.add(createKeyInterestRatesPanel());
		this.add(createPriceIndicesPanel());
		this.add(createMoneySupplyPanel());
		this.add(createMoneyCirculationPanel());
		this.add(createMoneyVelocityPanel());
		this.add(createCreditUtilizationRatePanel());
	}

	protected ChartPanel createCreditUtilizationRatePanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).creditUtilizationRateModel
							.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Credit Utilization Rate", "Date", "Credit Utilization Rate",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createKeyInterestRatesPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).keyInterestRateModel
							.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Key Interest Rate", "Date", "Key Interest Rate",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createPriceIndicesPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).priceIndexModel
							.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Price Index",
				"Date", "Price Index", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createMoneySupplyPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).moneySupplyM0Model
							.getTimeSeries());

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).moneySupplyM1Model
							.getTimeSeries());

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).moneySupplyM2Model
							.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Money Supply to Non-Banks", "Date", "Money Supply",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createMoneyCirculationPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).moneyCirculationModel
							.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Money Circulation", "Date", "Money Circulation",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createMoneyVelocityPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : Currency.values())
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).moneyVelocityModel
							.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Velocity of Money", "Date", "Velocity of Money",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	@Override
	public void notifyListener() {
	}
}