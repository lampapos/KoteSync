
#import <Foundation/Foundation.h>

@interface NSMutableDictionary (NotNilValue)

/*
Setting object to dictionary, if it' is not nil
 */
- (void)setObjectIfNotNil:(id)value forKey:(id)key;

@end