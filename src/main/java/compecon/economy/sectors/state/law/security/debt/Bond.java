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

package compecon.economy.sectors.state.law.security.debt;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.engine.Simulation;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.HourType;

@Entity
public abstract class Bond extends Property {

	@Column(name = "faceValue")
	protected double faceValue; // par value or principal

	@ManyToOne
	@JoinColumn(name = "issuerBankAccount_id")
	@Index(name = "issuerBankAccount")
	protected BankAccount issuerBankAccount;

	@Enumerated(value = EnumType.STRING)
	protected Currency issuedInCurrency;

	protected int termInYears = 1;

	@Transient
	protected List<ITimeSystemEvent> timeSystemEvents = new ArrayList<ITimeSystemEvent>();

	public void initialize() {
		super.initialize();

		// repay face value event;
		// has to be at HOUR_01, so that at HOUR_00 the last coupon can be payed
		ITimeSystemEvent transferFaceValueEvent = new TransferFaceValueEvent();
		this.timeSystemEvents.add(transferFaceValueEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(
						transferFaceValueEvent,
						Simulation.getInstance().getTimeSystem()
								.getCurrentYear()
								+ termInYears,
						Simulation.getInstance().getTimeSystem()
								.getCurrentMonthType(),
						Simulation.getInstance().getTimeSystem()
								.getCurrentDayType(), HourType.HOUR_01);
	}

	/*
	 * accessors
	 */

	public double getFaceValue() {
		return faceValue;
	}

	public BankAccount getIssuerBankAccount() {
		return issuerBankAccount;
	}

	public Currency getIssuedInCurrency() {
		return issuedInCurrency;
	}

	public int getTermInYears() {
		return termInYears;
	}

	public void setFaceValue(double faceValue) {
		this.faceValue = faceValue;
	}

	public void setIssuerBankAccount(BankAccount issuerBankAccount) {
		this.issuerBankAccount = issuerBankAccount;
	}

	public void setIssuedInCurrency(Currency issuedInCurrency) {
		this.issuedInCurrency = issuedInCurrency;
	}

	public void setTermInYears(int termInYears) {
		this.termInYears = termInYears;
	}

	/*
	 * business logic
	 */

	@Transient
	public void deconstruct() {
		// deregister from TimeSystem
		for (ITimeSystemEvent timeSystemEvent : this.timeSystemEvents)
			Simulation.getInstance().getTimeSystem()
					.removeEvent(timeSystemEvent);
	}

	public class TransferFaceValueEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Agent owner = PropertyRegister.getInstance().getOwner(Bond.this);
			Bond.this.issuerBankAccount.getManagingBank().transferMoney(
					Bond.this.issuerBankAccount,
					owner.getTransactionsBankAccount(), Bond.this.faceValue,
					"bond face value");
			Bond.this.deconstruct(); // delete bond from simulation
		}
	}

	public String toString() {
		return this.getClass().getSimpleName() + " [Issuer: "
				+ this.issuerBankAccount.getOwner() + ", Facevalue: "
				+ Currency.formatMoneySum(this.faceValue) + " "
				+ this.issuedInCurrency.getIso4217Code() + "]";
	}

}
