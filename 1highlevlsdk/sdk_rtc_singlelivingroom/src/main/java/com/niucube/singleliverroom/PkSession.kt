package com.niucube.singleliverroom

import com.niucube.absroom.seat.UserMicSeat

class PkSession( var initiatorLiver: UserMicSeat,
                 var receiverLiver: UserMicSeat,
                 //pk场次
                 var pkSessionId:String,
                  //pk扩展字段
                 var pkExtension:String)