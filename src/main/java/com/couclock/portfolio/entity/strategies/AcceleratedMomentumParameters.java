package com.couclock.portfolio.entity.strategies;

import javax.persistence.Entity;

@Entity
public class AcceleratedMomentumParameters extends StrategyParameters {

	public String usStock;
	public String exUsStock;
	public String bondStock;

	@Override
	public boolean equals(StrategyParameters otherParameters) {

		if (!(otherParameters instanceof AcceleratedMomentumParameters)) {
			return false;
		}

		AcceleratedMomentumParameters amp = (AcceleratedMomentumParameters) otherParameters;

		if (this.usStock.equals(amp.usStock) && this.exUsStock.equals(amp.exUsStock)
				&& this.bondStock.equals(amp.bondStock)) {
			return true;
		}

		return false;
	}

}
