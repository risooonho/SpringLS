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

package com.springrts.springls.commands.impl;


import com.springrts.springls.Account;
import com.springrts.springls.Client;
import com.springrts.springls.util.Misc;
import com.springrts.springls.commands.AbstractCommandProcessor;
import com.springrts.springls.commands.CommandProcessingException;
import com.springrts.springls.commands.SupportedCommand;
import java.net.InetAddress;
import java.util.List;

/**
 * Find a currently online client by IP, or if no match, search in the pool of
 * IPs last used by each account.
 * @author hoijui
 */
@SupportedCommand("FINDIP")
public class FindIpCommandProcessor extends AbstractCommandProcessor {

	public FindIpCommandProcessor() {
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

		boolean found = false;

		// NOTE In the past, this command allowed to specify a range of IPs,
		//   by specifying a radix like "192.168.*.*".
		//   Support for this has been removed.
		//   You now have to explicitly specify the IP.
		String ip = args.get(0);

		InetAddress addr = Misc.parseIp(ip);
		if (addr == null) {
			client.sendLine("SERVERMSG Invalid IP address: " + ip);
			return false;
		}

		for (int i = 0; i < getContext().getClients().getClientsSize(); i++) {
			Client curClient = getContext().getClients().getClient(i);
			if (!addr.equals(curClient.getIp())) {
				continue;
			}

			found = true;
			client.sendLine(String.format("SERVERMSG %s is bound to: %s",
					ip, curClient.getAccount().getName()));
		}

		// now let's check if this IP matches any recently used IP:
		Account lastAccount = getContext().getAccountsService()
				.findAccountByLastIP(addr);
		if ((lastAccount != null)
				&& !getContext().getClients().isUserLoggedIn(lastAccount))
		{
			found = true;
			client.sendLine(String.format(
					"SERVERMSG %s was recently bound to: %s (offline)",
					ip, lastAccount.getName()));
		}

		if (!found) {
			// TODO perhaps add an explanation like
			// "(note that server only keeps track of last used IP addresses)"?
			client.sendLine("SERVERMSG No client is/was recently using IP: "
					+ ip);
		}

		return true;
	}
}
