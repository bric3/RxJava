package rx.internal.operators;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Returns an Observable that ignore sequentially distinct items until one change according to the key selector or until completion occurs.
 * @param <T> the value type
 * @param <K> the key type
 */
public class OperatorIgnoreUntilChanged<T, K> implements Observable.Operator<T, T> {
    final Func1<? super T, ? extends K> keySelector;

    public OperatorIgnoreUntilChanged(Func1<? super T, ? extends K> keySelector) {
        this.keySelector = keySelector;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        return new Subscriber<T>() {
            K previousKey;
            T previous;
            boolean previousEmitted;
            boolean hasPrevious = false;

            @Override
            public void onNext(T current) {
                K key = keySelector.call(current);

                if (hasPrevious) {
                    if (this.previousKey != null && !this.previousKey.equals(key)) {
                        previousEmitted = true;
                        child.onNext(previous);
                    } else {
                        previousEmitted = false;
                        request(1);
                    }
                } else {
                    hasPrevious = true;
                }

                previous = current;
                previousKey = key;
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onCompleted() {
                if (hasPrevious || !previousEmitted) {
                    child.onNext(previous);
                }
                child.onCompleted();
            }
        };
    }
}
