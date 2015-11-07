/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var OrderModuleImpl = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/front/sess?views=true', function(sess, status, xhr) {
            this.resetTimeout(xhr);
            var contrMap = this.props.contrMap;
            var staging = this.staging;
            var marketMap = {};

            staging.views.clear();
            sess.views.forEach(function(view) {
                enrichView(contrMap, view);
                marketMap[view.market] = view.contr;
                staging.views.set(view.key, view);
            });

            staging.working.clear();
            staging.done.clear();
            sess.orders.forEach(function(order) {
                enrichOrder(contrMap, order);
                var orders = order.isDone ? staging.done : staging.working;
                orders.set(order.key, order);
            });

            staging.trades.clear();
            sess.trades.forEach(function(trade) {
                enrichTrade(contrMap, trade);
                staging.trades.set(trade.key, trade);
            });

            staging.posns.clear();
            sess.posns.forEach(function(posn) {
                enrichPosn(contrMap, posn);
                staging.posns.set(posn.key, posn);
            });

            this.setState({
                marketMap: marketMap,
                views: staging.views.toSortedArray(),
                sess: {
                    working: staging.working.toSortedArray(),
                    done: staging.done.toSortedArray(),
                    trades: staging.trades.toSortedArray(),
                    posns: staging.posns.toSortedArray()
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    updateSelected: function() {
        var isSelectedWorking = false;
        var isSelectedDone = false;
        var isSelectedTrade = false;
        var isSelectedArchivable = false;
        if (this.staging.context === 'working') {
            if (!this.staging.selectedWorking.isEmpty()) {
                isSelectedWorking = true;
            }
        } else if (this.staging.context === 'done') {
            if (!this.staging.selectedDone.isEmpty()) {
                isSelectedDone = true;
                isSelectedArchivable = true;
            }
        } else if (this.staging.context === 'trades') {
            if (!this.staging.selectedTrades.isEmpty()) {
                isSelectedTrade = true;
                isSelectedArchivable = true;
            }
        }
        this.setState({
            isSelectedWorking: isSelectedWorking,
            isSelectedDone: isSelectedDone,
            isSelectedTrade: isSelectedTrade,
            isSelectedArchivable: isSelectedArchivable
        });
    },
    postOrder: function(market, side, lots, price) {
        console.debug('postOrder: market=' + market + ', side=' + side
                       + ', lots=' + lots + ', price=' + price);
        var req = {};
        var contr = undefined;
        if (isSpecified(market)) {
            contr = this.state.marketMap[market];
        } else {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (contr === undefined) {
            this.onReportError(internalError('invalid market: ' + market));
            return;
        }
        if (isSpecified(side)) {
            req.side = side;
        } else {
            this.onReportError(internalError('side not specified'));
            return;
        }
        if (isSpecified(lots) && lots > 0) {
            req.lots = parseInt(lots);
        } else {
            this.onReportError(internalError('lots not specified'));
            return;
        }
        if (isSpecified(price)) {
            req.ticks = priceToTicks(price, contr);
        } else {
            this.onReportError(internalError('price not specified'));
            return;
        }

        $.ajax({
            type: 'post',
            url: '/back/sess/order/' + market,
            data: JSON.stringify(req)
        }).done(function(trans, status, xhr) {
            this.resetTimeout(xhr);
            this.applyTrans(trans);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    putOrder: function(market, ids, lots) {
        console.debug('putOrder: market=' + market + ', ids=[' + ids + '], lots=' + lots);
        var req = {};
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!Array.isArray(ids) || ids.length === 0) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        if (isSpecified(lots)) {
            req.lots = parseInt(lots);
        } else {
            this.onReportError(internalError('lots not specified'));
            return;
        }
        $.ajax({
            type: 'put',
            url: '/back/sess/order/' + market + '/' + ids,
            data: JSON.stringify(req)
        }).done(function(trans, status, xhr) {
            this.resetTimeout(xhr);
            this.applyTrans(trans);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    deleteOrder: function(market, ids) {
        console.debug('deleteOrder: market=' + market + ', id=' + ids);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!Array.isArray(ids) || ids.length === 0) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        $.ajax({
            type: 'delete',
            url: '/back/sess/order/' + market + '/' + ids
        }).done(function(unused, status, xhr) {
            this.resetTimeout(xhr);
            var done = this.staging.done;
            var sess = this.state.sess;
            ids.forEach(function(id) {
                var key = market + '/' + zeroPad(id);
                done.delete(key);
            }.bind(this));
            this.setState({
                sess: {
                    working: sess.working,
                    done: done.toSortedArray(),
                    trades: sess.trades,
                    posns: sess.posns
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    deleteTrade: function(market, ids) {
        console.debug('deleteTrade: market=' + market + ', ids=[' + ids + ']');
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!Array.isArray(ids) || ids.length === 0) {
            this.onReportError(internalError('trade-id not specified'));
            return;
        }
        $.ajax({
            type: 'delete',
            url: '/back/sess/trade/' + market + '/' + ids
        }).done(function(unused, status, xhr) {
            this.resetTimeout(xhr);
            var trades = this.staging.trades;
            var sess = this.state.sess;
            ids.forEach(function(id) {
                var key = market + '/' + zeroPad(id);
                trades.delete(key);
            }.bind(this));
            this.setState({
                sess: {
                    working: sess.working,
                    done: sess.done,
                    trades: trades.toSortedArray(),
                    posns: sess.posns
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    applyTrans: function(trans) {
        var contrMap = this.props.contrMap;
        var staging = this.staging;

        var views = staging.views;
        var working = staging.working;
        var done = staging.done;
        var trades = staging.trades;
        var posns = staging.posns;

        var view = trans.view;
        if (view !== null) {
            enrichView(contrMap, view);
            views.set(view.key, view);
        }
        trans.orders.forEach(function(order) {
            enrichOrder(contrMap, order);
            if (order.isDone) {
                working.delete(order.key);
                done.set(order.key, order);
            } else {
                working.set(order.key, order);
            }
        });
        trans.execs.forEach(function(exec) {
            if (exec.state === 'TRADE') {
                enrichTrade(contrMap, exec);
                trades.set(exec.key, exec);
            }
        });
        var posn = trans.posn;
        if (posn !== null) {
            enrichPosn(contrMap, posn);
            staging.posns.set(posn.key, posn);
        }
        this.setState({
            views: views.toSortedArray(),
            sess: {
                working: working.toSortedArray(),
                done: done.toSortedArray(),
                trades: trades.toSortedArray(),
                posns: posns.toSortedArray()
            }
        });
    },
    // DOM Events.
    onClearErrors: function() {
        console.debug('onClearErrors');
        var errors = this.staging.errors;
        errors.clear();
        this.setState({
            errors: errors.toArray()
        });
    },
    onReportError: function(error) {
        console.debug('onReportError: num=' + error.num + ', msg=' + error.msg);
        var errors = this.staging.errors;
        errors.push(error);
        this.setState({
            errors: errors.toArray()
        });
    },
    onRefresh: function() {
        console.debug('onRefresh');
        this.refresh();
    },
    onChangeContext: function(context) {
        console.debug('onChangeContext: context=' + context);
        this.staging.context = context;
        this.updateSelected();
    },
    onChangeFields: function(market, lots, price) {
        console.debug('onChangeFields: market=' + market + ', lots=' + lots + ', price=' + price);
        this.refs.newOrder.setFields(market, lots, price);
        this.refs.reviseOrder.setFields(market, lots, price);
    },
    onSelectOrder: function(order, isSelected) {
        console.debug('onSelectOrder: key=' + order.key + ', isSelected=' + isSelected);
        if (order.isDone) {
            var selectedDone = this.staging.selectedDone;
            if (isSelected) {
                selectedDone.set(order.key, order);
            } else {
                selectedDone.delete(order.key);
            }
        } else {
            var selectedWorking = this.staging.selectedWorking;
            if (isSelected) {
                selectedWorking.set(order.key, order);
            } else {
                selectedWorking.delete(order.key);
            }
        }
        this.updateSelected();
    },
    onSelectTrade: function(trade, isSelected) {
        console.debug('onSelectTrade: key=' + trade.key + ', isSelected=' + isSelected);
        var selectedTrades = this.staging.selectedTrades;
        if (isSelected) {
            selectedTrades.set(trade.key, trade);
        } else {
            selectedTrades.delete(trade.key);
        }
        this.updateSelected();
    },
    onPostOrder: function(market, side, lots, price) {
        this.postOrder(market, side, lots, price);
    },
    onReviseAll: function(lots) {
        if (this.staging.context === 'working') {
            var batches = new Map();
            this.staging.selectedWorking.forEach(function(key, order) {
                batches.push(order.market, order.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.putOrder(market, ids, lots);
            }.bind(this));
        }
    },
    onCancelAll: function() {
        if (this.staging.context === 'working') {
            var batches = new Map();
            this.staging.selectedWorking.forEach(function(key, order) {
                batches.push(order.market, order.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.putOrder(market, ids, 0);
            }.bind(this));
        }
    },
    onArchiveAll: function() {
        if (this.staging.context === 'done') {
            var batches = new Map();
            this.staging.selectedDone.forEach(function(key, order) {
                batches.push(order.market, order.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.deleteOrder(market, ids);
            }.bind(this));
        } else if (this.staging.context === 'trades') {
            var batches = new Map();
            this.staging.selectedTrades.forEach(function(key, trade) {
                batches.push(trade.market, trade.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.deleteTrade(market, ids);
            }.bind(this));
        }
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onRefresh: this.onRefresh,
                onChangeContext: this.onChangeContext,
                onChangeFields: this.onChangeFields,
                onSelectOrder: this.onSelectOrder,
                onSelectTrade: this.onSelectTrade,
                onPostOrder: this.onPostOrder,
                onReviseAll: this.onReviseAll,
                onCancelAll: this.onCancelAll,
                onArchiveAll: this.onArchiveAll
            },
            errors: [],
            marketMap: {},
            views: [],
            sess: {
                working: [],
                done: [],
                trades: [],
                posns: []
            },
            isSelectedWorking: false,
            isSelectedDone: false,
            isSelectedTrade: false,
            isSelectedArchivable: false
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var props = this.props;
        var contrMap = props.contrMap;

        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        var marketMap = state.marketMap;
        var views = state.views;
        var sess = state.sess;
        var isSelectedWorking = state.isSelectedWorking;
        var isSelectedDone = state.isSelectedDone;
        var isSelectedTrade = state.isSelectedTrade;
        var isSelectedArchivable = state.isSelectedArchivable;

        var marginTop = {
            marginTop: 24
        };

        return (
            <div className="orderModuleImpl">
              <MultiAlertWidget module={module} errors={errors}/>
              <NewOrderForm ref="newOrder" module={module} marketMap={marketMap}
                            isSelectedWorking={isSelectedWorking}/>
              <div style={marginTop}>
                <ViewTable module={module} views={views}/>
              </div>
              <div style={marginTop}>
                <ReviseOrderForm ref="reviseOrder" module={module} marketMap={marketMap}
                                 isSelectedArchivable={isSelectedArchivable}
                                 isSelectedWorking={isSelectedWorking}/>
              </div>
              <div style={marginTop}>
                <SessWidget module={module} sess={sess}/>
              </div>
            </div>
        );
    },
    resetTimeout: function(xhr) {
        var timeout = parseInt(xhr.getResponseHeader('Twirly-Timeout'));
        clearTimeout(this.timeout);
        if (timeout !== 0) {
            var delta = timeout - Date.now();
            this.timeout = setTimeout(function() {
                console.debug('timeout');
                $.getJSON('/back/task/poll', function(data, status, xhr) {
                    this.resetTimeout(xhr);
                }.bind(this));
            }.bind(this), delta);
        }
    },
    staging: {
        errors: new Tail(5),
        views: new Map(),
        working: new Map(),
        done: new Map(),
        trades: new Map(),
        posns: new Map(),
        selectedWorking: new Map(),
        selectedDone: new Map(),
        selectedTrades: new Map()
    },
    timeout: undefined
});

var OrderModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/front/rec/contr', function(contrs, status, xhr) {
            var contrMap = {};
            contrs.forEach(function(contr) {
                enrichContr(contr);
                contrMap[contr.mnem] = contr;
            });
            this.setState({
                contrMap: contrMap
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
        // App Engine work-around: this deliberate "ping" to the back-end ensures that it is alive
        // and ready to service trade requests.
        $.getJSON('/back/task/poll', function(data) { });
    },
    // DOM Events.
    // Lifecycle.
    getInitialState: function() {
        return {
            error: null,
            contrMap: null
        };
    },
    componentDidMount: function() {
        this.refresh();
    },
    render: function() {
        var state = this.state;
        var contrMap = state.contrMap;
        var error = state.error;
        var body = undefined;
        if (error !== null) {
            body = (
                <AlertWidget error={error}/>
            );
        } else if (contrMap !== null) {
            body = (
                <OrderModuleImpl contrMap={contrMap} pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="orderModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
