/*
	Copyright (c) 2011 Robin Vobruba <hoijui.quaero@gmail.com>

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
import com.springrts.tasserver.commands.AbstractCommandProcessor;
import com.springrts.tasserver.commands.CommandProcessingException;
import com.springrts.tasserver.commands.SupportedCommand;
import java.util.List;

/**
 * @author hoijui
 */
@SupportedCommand("GETLASTIP")
public class GetLastIpCommandProcessor extends AbstractCommandProcessor {

	public GetLastIpCommandProcessor() {
		super(1, 1, Account.Access.PRIVILEGED);
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

		Account acc = getContext().getAccountsService().getAccount(username);
		if (acc == null) {
			client.sendLine(new StringBuilder("SERVERMSG User ")
					.append(username).append(" not found!").toString());
			return false;
		}

		boolean online = getContext().getClients().isUserLoggedIn(acc);
		client.sendLine(new StringBuilder("SERVERMSG ")
				.append(username).append("'s last IP was ")
				.append(acc.getLastIpAsString()).append(" (")
				.append((online ? "online)" : "offline)")).toString());

		return true;
	}
}
