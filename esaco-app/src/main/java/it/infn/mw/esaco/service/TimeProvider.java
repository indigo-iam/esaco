/**
 * // SPDX-FileCopyrightText: 2026 Istituto Nazionale di Fisica Nucleare
 * //
 * // SPDX-License-Identifier: EUPL-1.2
 */
package it.infn.mw.esaco.service;

/***
 * 
 * Interface that provides the current time.
 *
 */
@FunctionalInterface
public interface TimeProvider {

  /***
   * Return the current time, in milliseconds, from the Epoch.
   * 
   * @return Current timestamp
   */
  long currentTimeMillis();

}
