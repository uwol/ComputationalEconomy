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

package compecon.math;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.engine.Simulation;
import compecon.engine.statistics.Log;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.math.price.IPriceFunction;

public abstract class ConvexFunction<T> extends Function<T> {

	public enum ConvexFunctionTerminationCause {
		INPUT_FACTOR_UNAVAILABLE, NO_OPTIMAL_INPUT, BUDGET_SPENT;
	}

	protected ConvexFunction(
			boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	@Override
	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, IPriceFunction> priceFunctionsOfInputGoods,
			final double budget) {
		return this.calculateOutputMaximizingInputsIterative(
				priceFunctionsOfInputGoods, budget,
				ConfigurationUtil.MathConfig.getNumberOfIterations());
	}

	/**
	 * calculates the output maximizing bundle of inputs under a budget
	 * restriction and given static markets prices of inputs.
	 * 
	 * @param budget
	 *            determines the granularity of the output, as the budget is
	 *            divided by the numberOfIterations -> large budget leads to
	 *            large chunks
	 */
	public Map<T, Double> calculateOutputMaximizingInputsIterative(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget, final int numberOfIterations) {

		assert (numberOfIterations > 0);

		// check if inputs have NaN prices
		boolean pricesAreNaN = false;
		for (T inputType : this.getInputTypes()) {
			if (Double.isNaN(priceFunctionsOfInputTypes.get(inputType)
					.getPrice(0.0))) {
				pricesAreNaN = true;
				break;
			}
		}

		/*
		 * special cases
		 */

		// special case: if some prices are NaN, then not all inputs can be set.
		// This becomes a problem, if all inputs have to be set -> return zero
		// input
		if (pricesAreNaN && this.needsAllInputFactorsNonZeroForPartialDerivate) {
			getLog().log(
					"at least one of the prices is Double.NaN, but the function needs all inputs set -> no calculation");
			getLog().agent_onCalculateOutputMaximizingInputsIterative(budget,
					0.0,
					ConvexFunctionTerminationCause.INPUT_FACTOR_UNAVAILABLE);

			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.lesserEqual(budget, 0.0)) {
			getLog().log("budget is " + budget + " -> no calculation");
			getLog().agent_onCalculateOutputMaximizingInputsIterative(budget,
					0.0, ConvexFunctionTerminationCause.BUDGET_SPENT);

			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		/*
		 * regular calculation
		 */
		double moneySpent = 0.0;
		final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

		// initialize
		if (this.getNeedsAllInputFactorsNonZeroForPartialDerivate()) {
			for (T inputType : this.getInputTypes()) {
				bundleOfInputs.put(inputType, 0.0000001);
				moneySpent += bundleOfInputs.get(inputType)
						* priceFunctionsOfInputTypes.get(inputType).getPrice(
								bundleOfInputs.get(inputType));
			}
		} else {
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
		}

		// maximize output
		final int NUMBER_OF_ITERATIONS = bundleOfInputs.size()
				* numberOfIterations;

		while (true) {
			// if the budget is spent
			if (MathUtil.greater(moneySpent, budget)) {
				getLog().log("budget spent completely");
				getLog().agent_onCalculateOutputMaximizingInputsIterative(
						budget, moneySpent,
						ConvexFunctionTerminationCause.BUDGET_SPENT);
				break;
			}

			T optimalInputType = this.findHighestPartialDerivatePerPrice(
					bundleOfInputs, priceFunctionsOfInputTypes);

			// no optimal input type could be found, i. e. markets are sold out
			if (optimalInputType == null) {
				getLog().log("no optimal input found -> terminating");
				getLog().agent_onCalculateOutputMaximizingInputsIterative(
						budget, moneySpent,
						ConvexFunctionTerminationCause.NO_OPTIMAL_INPUT);
				break;
			} else {
				double marginalPriceOfOptimalInputType = priceFunctionsOfInputTypes
						.get(optimalInputType).getMarginalPrice(
								bundleOfInputs.get(optimalInputType));
				double amount = (budget / (double) NUMBER_OF_ITERATIONS)
						/ marginalPriceOfOptimalInputType;
				bundleOfInputs.put(optimalInputType,
						bundleOfInputs.get(optimalInputType) + amount);
				moneySpent += marginalPriceOfOptimalInputType * amount;
			}
		}

		return bundleOfInputs;
	}

	private Log getLog() {
		return Simulation.getInstance().getLog();
	}
}
