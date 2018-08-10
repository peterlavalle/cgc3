
#ifndef _pal_signal_macros_
#define _pal_signal_macros_

#define SIGNAL(NAME) struct NAME
#define INPUT(NAME, KIND) KIND NAME;
#define OUTPUT(NAME, KIND) KIND NAME;
#define EVENT(NAME) void NAME(void);

#endif // _pal_signal_macros_
