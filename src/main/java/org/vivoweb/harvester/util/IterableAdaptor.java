/**
 * Copyright (c) 2010, Cornell University All rights reserved. Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions are met: Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither the name of Cornell University nor the
 * names of its contributors may be used to endorse or promote products derived from this software without specific
 * prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.vivoweb.harvester.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapts Enumerations and Iterators to be Iterable
 * @param <T> type
 */
public class IterableAdaptor<T> implements Iterable<T> {
	/**
	 * enumeration
	 */
	private final Enumeration<T> en;
	/**
	 * iterator
	 */
	private final Iterator<T> it;
	
	/**
	 * sometimes you have an Enumeration and you want an Iterable
	 * @param en enumeration to adapt
	 */
	public IterableAdaptor(Enumeration<T> en) {
		this.en = en;
		this.it = null;
	}
	
	/**
	 * Accessor for en
	 * @return en
	 */
	protected final Enumeration<T> getEn() {
		return this.en;
	}
	
	/**
	 * sometimes you have an Iterator but you want to use a for
	 * @param it iterator to adapt
	 */
	public IterableAdaptor(Iterator<T> it) {
		this.it = it;
		this.en = null;
	}
	
	// return an adaptor for the Enumeration
	@Override
	public Iterator<T> iterator() {
		if(this.en != null) {
			return new Iterator<T>() {
				@Override
				public boolean hasNext() {
					return getEn().hasMoreElements();
				}
				
				@Override
				public T next() {
					return getEn().nextElement();
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if(this.it != null) {
			return this.it;
		} else {
			return new Iterator<T>() {
				@Override
				public boolean hasNext() {
					return false;
				}
				
				@Override
				public T next() {
					return null;
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
	}
	
	/**
	 * sometimes you have an Enumeration and you want an Iterable
	 * @param <T> type
	 * @param enin enumeration to adapt
	 * @return an iterable adapter for the enumeration
	 */
	public static <T> Iterable<T> adapt(Enumeration<T> enin) {
		return new IterableAdaptor<T>(enin);
	}
	
	/**
	 * sometimes you have an Iterator but you want to use a for
	 * @param <T> type
	 * @param itin iterator to adapt
	 * @return an iterable adapter for the iterator
	 */
	public static <T> Iterable<T> adapt(Iterator<T> itin) {
		return new IterableAdaptor<T>(itin);
	}
}
