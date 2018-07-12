/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * 
 * This file is part of DroidUPNP.
 * 
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tti.ttimediastore.dlna.dms.controller.cling;

import android.content.Context;

import com.tti.ttimediastore.dlna.dms.controller.upnp.IUpnpServiceController;
import com.tti.ttimediastore.dlna.dms.model.upnp.IFactory;

public class Factory implements IFactory {

	@Override
	public IUpnpServiceController createUpnpServiceController(Context ctx)
	{
		return new ServiceController(ctx);
	}
}
