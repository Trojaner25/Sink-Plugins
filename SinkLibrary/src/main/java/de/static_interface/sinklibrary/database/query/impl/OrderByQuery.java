/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinklibrary.database.query.impl;

import de.static_interface.sinklibrary.database.Row;
import de.static_interface.sinklibrary.database.query.Order;
import de.static_interface.sinklibrary.database.query.Query;

public class OrderByQuery<T extends Row> extends Query<T> {

    private final String column;
    private Order order;

    public OrderByQuery(Query<T> parent, String column, Order order) {
        super(parent);
        this.column = column;
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public String getColumn() {
        return column;
    }
}
