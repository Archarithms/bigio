#!/usr/bin/python

import unittest

class TestTest(unittest.TestCase):
    def setUp(self):
        print 'Setting up'

    def test_case1(self):
        print 'Test case 1'

if __name__ == '__main__':
    unittest.main()

