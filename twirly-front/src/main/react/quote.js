/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var QuoteModuleImpl = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/front/sess/quote', function(quotes, status, xhr) {
            this.resetTimeout(xhr);
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            staging.quotes.clear();
            quotes.forEach(function(quote) {
                enrichQuote(contrMap, quote);
                staging.quotes.set(quote.key, quote);
            });

            this.setState({
                quotes: staging.quotes.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    postQuote: function(market, side, lots) {
        console.debug('postQuote: market=' + market + ', side=' + side
                       + ', lots=' + lots);
        var req = {};
        var contr = undefined;
        if (isSpecified(market)) {
            contr = this.props.marketMap[market];
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

        $.ajax({
            type: 'post',
            url: '/back/sess/quote/' + market,
            data: JSON.stringify(req)
        }).done(function(quote, status, xhr) {

            var contrMap = this.props.contrMap;
            var staging = this.staging;

            var quotes = staging.quotes;

            this.resetTimeout(xhr);
            enrichQuote(contrMap, quote);
            quotes.set(quote.key, quote);
            this.setState({
                quotes: quotes.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
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
    onPostQuote: function(market, side, lots) {
        this.postQuote(market, side, lots);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onPostQuote: this.onPostQuote
            },
            errors: [],
            quotes: []
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var props = this.props;
        var marketMap = props.marketMap;

        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        var quotes = state.quotes;

        var marginTop = {
            marginTop: 24
        };

        return (
            <div className="quoteModuleImpl">
              <MultiAlertWidget module={module} errors={errors}/>
              <NewQuoteForm ref="newOrder" module={module} marketMap={marketMap}/>
              <div style={marginTop}>
                <QuoteTable module={module} quotes={quotes}/>
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
        quotes: new Map()
    },
    timeout: undefined
});

var QuoteModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/front/rec/contr,market', function(rec, status, xhr) {
            var contrMap = {};
            var marketMap = {};
            rec.contrs.forEach(function(contr) {
                enrichContr(contr);
                contrMap[contr.mnem] = contr;
            });
            rec.markets.forEach(function(market) {
                enrichMarket(contrMap, market);
                marketMap[market.key] = market.contr;
            });
            this.setState({
                contrMap: contrMap,
                marketMap: marketMap
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
            contrMap: null,
            marketMap: null
        };
    },
    componentDidMount: function() {
        this.refresh();
    },
    render: function() {
        var state = this.state;
        var contrMap = state.contrMap;
        var marketMap = state.marketMap;
        var error = state.error;
        var body = undefined;
        if (error !== null) {
            body = (
                <AlertWidget error={error}/>
            );
        } else if (contrMap !== null) {
            body = (
                <QuoteModuleImpl contrMap={contrMap} marketMap={marketMap}
                                 pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="quoteModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
