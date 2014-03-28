/*
 * Copyright (c) 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.mojang.api.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

/*
    TODO: refactor so unit tests can be written :)
 */
public class BasicHttpClient implements HttpClient
{

    private static BasicHttpClient instance;

    public BasicHttpClient()
    {
    }

    public static BasicHttpClient getInstance()
    {
        if ( instance == null )
        {
            instance = new BasicHttpClient();
        }
        return instance;
    }

    @Override
    public String post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException
    {
        return post(url, null, body, headers);
    }

    @Override
    public String post(URL url, Proxy proxy, HttpBody body, List<HttpHeader> headers) throws IOException
    {
        if ( proxy == null ) proxy = Proxy.NO_PROXY;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setRequestMethod("POST");

        for ( HttpHeader header : headers )
        {
            connection.setRequestProperty(header.getName(), header.getValue());
        }

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(body.getBytes());
        writer.flush();
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();

        while ( (line = reader.readLine()) != null )
        {
            response.append(line);
            response.append('\r');
        }

        reader.close();
        return response.toString();
    }
}