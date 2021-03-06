/*
 * Copyright 2014 Commonwealth Computer Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.locationtech.geomesa.utils.time

import java.util.Date

import org.joda.time.{DateTime, Interval}

object Time {
  val noInterval: Interval = null

  implicit class RichInterval(self: Interval) {
    def getSafeUnion(other: Interval): Interval = {
      if (self != noInterval && other != noInterval) {
        new Interval(
          if (self.getStart.isBefore(other.getStart)) self.getStart else other.getStart,
          if (self.getEnd.isAfter(other.getEnd)) self.getEnd else other.getEnd
        )
      } else noInterval
    }

    def getSafeIntersection(other: Interval): Interval =
      if (self == noInterval) other
      else if (other == noInterval) self
      else {
        new Interval(
          if (self.getStart.isBefore(other.getStart)) other.getStart else self.getStart,
          if (self.getEnd.isAfter(other.getEnd)) other.getEnd else self.getEnd
        )
      }

    def expandByDate(date: Date): Interval = {
      if      (date == null) self
      else if (self == null) new Interval(new DateTime(date), new DateTime(date))
      else                   expandByDateTimeNoChecks(new DateTime(date))
    }

    private def expandByDateTime(dt: DateTime): Interval = {
      if      (dt == null)   self
      else if (self == null) new Interval(dt, dt)
      else                   expandByDateTimeNoChecks(dt)
    }

    private def expandByDateTimeNoChecks(dt: DateTime): Interval = {
      if      (self.isBefore(dt)) self.withEnd(dt)
      else if (self.isAfter(dt))  self.withStart(dt)
      else                        self
    }

    def expandByInterval(interval: Interval) =
      if (interval == null)  self
      else if (self == null) interval
      else new RichInterval(expandByDateTime(interval.getStart)).expandByDateTime(interval.getEnd)
  }
}
