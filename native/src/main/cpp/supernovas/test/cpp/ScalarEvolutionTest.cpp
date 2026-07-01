/**
 * @file
 *
 * @date Created  on Oct 13, 2025
 * @author Attila Kovacs
 */

#include <iostream>

#include "TestUtil.hpp"



int main() {
  TestUtil test = TestUtil("ScalarEvolution");

  int n = 0;


  if(!test.check("invalid(pos)", !ScalarEvolution(NAN, -2.0, 3.0).is_valid())) n++;
  if(!test.check("invalid(vel)", !ScalarEvolution(1.0, NAN, 3.0).is_valid())) n++;
  if(!test.check("invalid(acc)", !ScalarEvolution(1.0, -2.0, NAN).is_valid())) n++;


  ScalarEvolution e(1.0, -2.0, 3.0);
  if(!test.check("is_valid()", e.is_valid())) n++;
  if(!test.equals("value(0.0)", e.value(), 1.0)) n++;
  if(!test.equals("rate(0.0)", e.rate(), -2.0)) n++;
  if(!test.equals("acceleration(0.0)", e.acceleration(), 3.0)) n++;
  if(!test.equals("value(1.0)", e.value(Interval(1.0)), 2.0)) n++;
  if(!test.equals("rate(1.0)", e.rate(Interval(1.0)), 1.0)) n++;
  if(!test.equals("value(-1.0)", e.value(Interval(-1.0)), 6.0)) n++;
  if(!test.equals("rate(-1.0)", e.rate(Interval(-1.0)), -5.0)) n++;

  if(!test.check("stationary(NAN)", !ScalarEvolution::stationary(NAN).is_valid())) n++;

  e = ScalarEvolution::stationary(1.23);
  if(!test.check("stationary()", e.is_valid())) n++;
  if(!test.equals("value(stationary)", e.value(Interval(1.0)), 1.23)) n++;
  if(!test.equals("rate(stationary)", e.rate(Interval(1.0)), 0.0)) n++;
  if(!test.equals("acceleration(stationary)", e.acceleration(), 0.0)) n++;

  std::cout << "ScalarEvolution.cpp: " << (n > 0 ? "FAILED" : "OK") << "\n";
  return n;
}
