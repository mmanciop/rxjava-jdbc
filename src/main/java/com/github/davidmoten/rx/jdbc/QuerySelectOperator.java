package com.github.davidmoten.rx.jdbc;

import java.sql.ResultSet;

import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.functions.Func1;

import com.github.davidmoten.rx.OperatorFromOperation;

/**
 * Operator corresponding to the QuerySelectOperation.
 * 
 * @param <T>
 */
public class QuerySelectOperator<T,R> implements Operator<T, R> {

    private final OperatorFromOperation<T, R> operator;

    /**
     * Constructor.
     * 
     * @param builder
     * @param function
     * @param operatorType
     */
    QuerySelectOperator(final QuerySelect.Builder builder, final Func1<ResultSet, T> function,
            final OperatorType operatorType) {
        operator = new OperatorFromOperation<T, R>(new Func1<Observable<R>, Observable<T>>() {

            @SuppressWarnings("rawtypes")
			@Override
            public Observable<T> call(Observable<R> observable) {
                if (operatorType == OperatorType.PARAMETER)
                    return builder.parameters(observable).get(function);
                else if (operatorType==OperatorType.DEPENDENCY)
                    // dependency
                    return builder.dependsOn(observable).get(function);
                else //PARAMETER_LIST
                	return observable.cast(Observable.class).flatMap(new Func1<Observable,Observable<T>>(){
						@SuppressWarnings("unchecked")
						@Override
						public Observable<T> call(Observable parameters) {
							return builder.parameters(parameters).get(function);
						}});
            }
        });
    }

    @Override
    public Subscriber<? super R> call(Subscriber<? super T> subscriber) {
        return operator.call(subscriber);
    }
}
