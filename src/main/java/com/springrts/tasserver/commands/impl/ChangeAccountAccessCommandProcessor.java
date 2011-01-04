/*
	Copyright (c) 2010 Robin Vobruba <robin.vobruba@derisk.ch>

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.springrts.tasserver.commands.impl;


import com.springrts.tasserver.Account;
import com.springrts.tasserver.Client;
import com.springrts.tasserver.ServerNotification;
import com.springrts.tasserver.commands.AbstractCommandProcessor;
import com.springrts.tasserver.commands.CommandProcessingException;
import com.springrts.tasserver.commands.SupportedCommand;
import java.util.List;

/**
 * @author hoijui
 */
@SupportedCommand("CHANGEACCOUNTACCESS")
public class ChangeAccountAccessCommandProcessor extends AbstractCommandProcessor {

	public ChangeAccountAccessCommandProcessor() {
		super(2, 2, Account.Access.ADMIN);
	}

	@Override
	public boolean process(Client client, List<String> args)
			throws CommandProcessingException
	{
		boolean checksOk = super.process(client, args);
		if (!checksOk) {
			return false;
		}

		String username = args.get(0);
		String accessBitsString = args.get(1);

		int newAccessBifField = -1;
		try {
			newAccessBifField = Integer.parseInt(accessBitsString);
		} catch (NumberFormatException e) {
			return false;
		}

		Account acc = getContext().getAccountsService().getAccount(username);
		if (acc == null) {
			return false;
		}

		int oldAccessBitField = acc.getAccessBitField();
		Account account_new = acc.clone();
		account_new.setAccess(Account.extractAccess(newAccessBifField));
		account_new.setBot(Account.extractBot(newAccessBifField));
		account_new.setInGameTime(Account.extractInGameTime(newAccessBifField));
		account_new.setAgreementAccepted(Account.extractAgreementAccepted(newAccessBifField));
		final boolean mergeOk = getContext().getAccountsService().mergeAccountChanges(account_new, account_new.getName());
		if (mergeOk) {
			acc = account_new;
		} else {
			client.sendLine(new StringBuilder("SERVERMSG Changing ACCESS for account <")
					.append(acc.getName()).append("> failed.").toString());
			return false;
		}

		getContext().getAccountsService().saveAccounts(false); // save changes
		// just in case if rank got changed: FIXME?
		//Client target=context.getClients().getClient(commands[1]);
		//target.setRankToStatus(client.account.getRank().ordinal());
		//if(target.alive)
		//	context.getClients().notifyClientsOfNewClientStatus(target);

		client.sendLine(new StringBuilder("SERVERMSG You have changed ACCESS for <")
				.append(acc.getName()).append("> successfully.").toString());

		// add server notification:
		ServerNotification sn = new ServerNotification("Account access changed by admin");
		sn.addLine(new StringBuilder("Admin <")
				.append(client.getAccount().getName()).append("> has changed access/status bits for account <")
				.append(acc.getName()).append(">.").toString());
		sn.addLine(new StringBuilder("Old access code: ")
				.append(oldAccessBitField).append(". New code: ")
				.append(newAccessBifField).toString());
		getContext().getServerNotifications().addNotification(sn);

		return true;
	}
}